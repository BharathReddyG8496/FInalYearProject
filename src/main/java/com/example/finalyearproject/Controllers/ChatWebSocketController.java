package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Services.ChatService;
import com.example.finalyearproject.Utility.ChatMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    @Autowired
    private ChatService chatService;

    /**
     * Handle WebSocket messages sent to /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage,
                            SimpMessageHeaderAccessor headerAccessor,
                            Principal principal) {
        try {
            // Verify sender matches authenticated user
            if (!principal.getName().equals(chatMessage.getSenderEmail())) {
                logger.warn("Attempted message spoofing: {} tried to send as {}",
                        principal.getName(), chatMessage.getSenderEmail());
                return;
            }

            // Extract role from headers (you might need to customize this based on your security setup)
            String senderRole = headerAccessor.getUser().toString().contains("FARMER") ? "FARMER" : "CONSUMER";

            // Process and save the message
            chatService.sendMessage(
                    chatMessage.getContent(),
                    chatMessage.getSenderEmail(),
                    chatMessage.getReceiverEmail(),
                    senderRole,
                    chatMessage.getProductId()
            );

            // The message will be broadcast by the Kafka consumer service
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: {}", e.getMessage(), e);
        }
    }
}
