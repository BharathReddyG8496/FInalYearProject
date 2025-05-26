package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Services.ChatService;
import com.example.finalyearproject.Utility.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    // Add to ChatController
    @PostMapping("/product/{productId}/initiate")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<ChatSessionSummaryDTO>> initiateProductChat(
            @PathVariable int productId,
            @RequestParam String initialMessage,
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<ChatSessionSummaryDTO> response = chatService.initiateProductChat(
                productId, initialMessage, consumerEmail);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Send a message
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyAuthority('CONSUMER', 'FARMER')")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(
            @RequestParam @NotBlank String receiverEmail,
            @RequestParam @NotBlank String content,
            @RequestParam(required = false) Integer productId,
            Authentication authentication) {

        String senderEmail = authentication.getName();
        String senderRole = authentication.getAuthorities().iterator().next().getAuthority();

        logger.info("Message send request from {} to {}", senderEmail, receiverEmail);

        ApiResponse<ChatMessageDTO> response = chatService.sendMessage(
                content, senderEmail, receiverEmail, senderRole, productId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get messages for a specific chat session
     */
    @GetMapping("/messages/{sessionId}")
    @PreAuthorize("hasAnyAuthority('CONSUMER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getMessages(
            @PathVariable Long sessionId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Getting messages for session {}, user {}", sessionId, userEmail);

        ApiResponse<List<ChatMessageDTO>> response = chatService.getChatMessages(sessionId, userEmail);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all chat sessions for the authenticated user
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasAnyAuthority('CONSUMER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<ChatSessionSummaryDTO>>> getSessions(Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        logger.info("Getting chat sessions for user {}, role {}", email, role);

        ApiResponse<List<ChatSessionSummaryDTO>> response = chatService.getChatSessions(email, role);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/mark-read")
    @PreAuthorize("hasAnyAuthority('CONSUMER', 'FARMER')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestBody List<Long> messageIds,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Marking messages as read: {}, user: {}", messageIds, userEmail);

        ApiResponse<Void> response = chatService.markMessagesAsRead(messageIds, userEmail);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get unread messages
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyAuthority('CONSUMER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getUnreadMessages(Authentication authentication) {
        String email = authentication.getName();
        logger.info("Getting unread messages for user {}", email);

        ApiResponse<List<ChatMessageDTO>> response = chatService.getUnreadMessages(email);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all farmers for chat
     */
    @GetMapping("/farmers")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<FarmerSummaryDTO>>> getAllFarmers() {
        logger.info("Getting all farmers for chat");

        ApiResponse<List<FarmerSummaryDTO>> response = chatService.getAllFarmersForChat();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all consumers for chat (for farmers)
     */
    @GetMapping("/consumers")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<ConsumerSummaryDTO>>> getAllConsumers() {
        logger.info("Getting all consumers for chat");

        ApiResponse<List<ConsumerSummaryDTO>> response = chatService.getAllConsumersForChat();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
