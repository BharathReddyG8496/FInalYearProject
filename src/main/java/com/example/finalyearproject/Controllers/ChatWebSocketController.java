package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Services.ChatService;
import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.Utility.ChatMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, Principal principal) {
        try {
            if (principal == null) {
                logger.error("No authenticated user for WebSocket message");
                return;
            }

            String senderEmail = principal.getName();

            // Verify sender matches authenticated user
            if (!senderEmail.equals(chatMessage.getSenderEmail())) {
                logger.warn("Attempted message spoofing: {} tried to send as {}",
                        senderEmail, chatMessage.getSenderEmail());
                return;
            }

            // Extract role from authentication
            String senderRole = "CONSUMER"; // default
            if (principal instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
                if (!auth.getAuthorities().isEmpty()) {
                    senderRole = auth.getAuthorities().iterator().next().getAuthority();
                }
            }

            logger.info("Processing WebSocket message from {} (role: {}) to {}",
                    senderEmail, senderRole, chatMessage.getReceiverEmail());

            // Process and save the message
            ApiResponse<ChatMessageDTO> response = chatService.sendMessage(
                    chatMessage.getContent(),
                    chatMessage.getSenderEmail(),
                    chatMessage.getReceiverEmail(),
                    senderRole,
                    chatMessage.getProductId()
            );

            if (!response.isSuccess()) {
                logger.error("Failed to save message: {}", response.getMessage());
                // Send error back to sender
                messagingTemplate.convertAndSendToUser(
                        senderEmail,
                        "/queue/errors",
                        "Failed to send message: " + response.getMessage()
                );
            }

        } catch (Exception e) {
            logger.error("Error processing WebSocket message: {}", e.getMessage(), e);
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(
                        principal.getName(),
                        "/queue/errors",
                        "Error sending message"
                );
            }
        }
    }
}