package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.*;
import com.example.finalyearproject.Configs.KafkaConfig;
import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.Utility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatMessageRepo chatMessageRepo;

    @Autowired
    private ChatSessionRepo chatSessionRepo;

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private KafkaTemplate<String, ChatMessageDTO> kafkaTemplate;

    @Autowired
    private ProductRepo productRepo;

    /**
     * Send a message and publish to Kafka
     */
    @Transactional
    public ApiResponse<ChatMessageDTO> sendMessage(String content, String senderEmail,
                                                   String receiverEmail, String senderRole,
                                                   Integer productId) {
        try {
            logger.info("Sending message from {} to {}", senderEmail, receiverEmail);

            // Get or create chat session
            ChatSession session = getOrCreateChatSession(senderEmail, receiverEmail, senderRole);

            // Create and save message
            ChatMessage message = productId != null ?
                    ChatMessage.create(content, senderEmail, receiverEmail, senderRole, session, productId) :
                    ChatMessage.create(content, senderEmail, receiverEmail, senderRole, session);

            message = chatMessageRepo.save(message);

            // Update session with last message preview and timestamp
            session.updateLastMessagePreview(content);

            // Update unread counts
            if ("CONSUMER".equals(senderRole)) {
                session.setFarmerUnreadCount(session.getFarmerUnreadCount() + 1);
            } else {
                session.setConsumerUnreadCount(session.getConsumerUnreadCount() + 1);
            }

            chatSessionRepo.save(session);

            // Convert to DTO
            ChatMessageDTO messageDTO = convertToDTO(message);

            // Publish to Kafka
            kafkaTemplate.send(KafkaConfig.TOPIC_CHAT_MESSAGES, messageDTO);
            logger.info("Message sent and published to Kafka: {}", messageDTO.getId());

            return ApiResponse.success("Message sent successfully", messageDTO);
        } catch (Exception e) {
            logger.error("Failed to send message", e);
            return ApiResponse.error("Failed to send message", e.getMessage());
        }
    }

    /**
     * Get chat messages for a session
     */
    public ApiResponse<List<ChatMessageDTO>> getChatMessages(Long sessionId, String userEmail) {
        try {
            // Verify session exists and user is a participant
            Optional<ChatSession> sessionOpt = chatSessionRepo.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                return ApiResponse.error("Chat session not found", "No session with ID: " + sessionId);
            }

            ChatSession session = sessionOpt.get();
            if (!isParticipant(session, userEmail)) {
                return ApiResponse.error("Access denied", "You are not a participant in this chat");
            }

            List<ChatMessage> messages = chatMessageRepo.findByChatSessionIdOrderByTimestampAsc(sessionId);
            List<ChatMessageDTO> messageDTOs = messages.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // Mark messages as read
            boolean isConsumer = session.getConsumerEmail().equals(userEmail);
            if (isConsumer) {
                int updated = chatMessageRepo.markAllSessionMessagesAsRead(userEmail, sessionId);
                if (updated > 0) {
                    chatSessionRepo.updateConsumerUnreadCount(sessionId, 0);
                }
            } else {
                int updated = chatMessageRepo.markAllSessionMessagesAsRead(userEmail, sessionId);
                if (updated > 0) {
                    chatSessionRepo.updateFarmerUnreadCount(sessionId, 0);
                }
            }

            return ApiResponse.success("Chat messages retrieved", messageDTOs);
        } catch (Exception e) {
            logger.error("Failed to retrieve messages", e);
            return ApiResponse.error("Failed to retrieve messages", e.getMessage());
        }
    }

    /**
     * Get all chat sessions for a user
     */
    public ApiResponse<List<ChatSessionSummaryDTO>> getChatSessions(String email, String role) {
        try {
            List<ChatSession> sessions = chatSessionRepo.findAllSessionsByUser(email);
            List<ChatSessionSummaryDTO> sessionDTOs = sessions.stream()
                    .map(session -> convertToSessionSummaryDTO(session, email, role))
                    .collect(Collectors.toList());

            return ApiResponse.success("Chat sessions retrieved", sessionDTOs);
        } catch (Exception e) {
            logger.error("Failed to retrieve chat sessions", e);
            return ApiResponse.error("Failed to retrieve chat sessions", e.getMessage());
        }
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public ApiResponse<Void> markMessagesAsRead(List<Long> messageIds, String userEmail) {
        try {
            // Verify user has access to these messages
            for (Long id : messageIds) {
                Optional<ChatMessage> msgOpt = chatMessageRepo.findById(id);
                if (msgOpt.isPresent()) {
                    ChatMessage message = msgOpt.get();
                    // Can only mark as read if user is the receiver
                    if (!message.getReceiverEmail().equals(userEmail)) {
                        return ApiResponse.error("Access denied", "You can only mark messages sent to you as read");
                    }
                }
            }

            chatMessageRepo.markMessagesAsRead(messageIds);

            // Update unread counts for sessions
            updateUnreadCountsForSessions(userEmail);

            return ApiResponse.success("Messages marked as read");
        } catch (Exception e) {
            logger.error("Failed to mark messages as read", e);
            return ApiResponse.error("Failed to mark messages as read", e.getMessage());
        }
    }

    /**
     * Get unread messages for a user
     */
    public ApiResponse<List<ChatMessageDTO>> getUnreadMessages(String email) {
        try {
            List<ChatMessage> unreadMessages = chatMessageRepo.findUnreadMessagesByReceiver(email);
            List<ChatMessageDTO> messageDTOs = unreadMessages.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success("Unread messages retrieved", messageDTOs);
        } catch (Exception e) {
            logger.error("Failed to retrieve unread messages", e);
            return ApiResponse.error("Failed to retrieve unread messages", e.getMessage());
        }
    }

    /**
     * Get farmers for chat (all farmers)
     */
    public ApiResponse<List<FarmerSummaryDTO>> getAllFarmersForChat() {
        try {
            List<Farmer> farmers = farmerRepo.findAll();
            List<FarmerSummaryDTO> farmerDTOs = farmers.stream()
                    .map(this::convertToFarmerSummaryDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success("Farmers retrieved successfully", farmerDTOs);
        } catch (Exception e) {
            logger.error("Failed to retrieve farmers for chat", e);
            return ApiResponse.error("Failed to retrieve farmers", e.getMessage());
        }
    }

    /**
     * Get consumers for chat (all consumers - for farmers to see)
     */
    public ApiResponse<List<ConsumerSummaryDTO>> getAllConsumersForChat() {
        try {
            List<Consumer> consumers = consumerRepo.findAll();
            List<ConsumerSummaryDTO> consumerDTOs = consumers.stream()
                    .map(this::convertToConsumerSummaryDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success("Consumers retrieved successfully", consumerDTOs);
        } catch (Exception e) {
            logger.error("Failed to retrieve consumers for chat", e);
            return ApiResponse.error("Failed to retrieve consumers", e.getMessage());
        }
    }

    /**
     * Get or create a chat session
     */
    private ChatSession getOrCreateChatSession(String email1, String email2, String senderRole) {
        // Determine consumer and farmer email based on sender role
        String consumerEmail, farmerEmail;
        if ("CONSUMER".equals(senderRole)) {
            consumerEmail = email1;
            farmerEmail = email2;
        } else {
            consumerEmail = email2;
            farmerEmail = email1;
        }

        // Try to find existing session
        Optional<ChatSession> existingSession = chatSessionRepo.findByConsumerAndFarmer(consumerEmail, farmerEmail);

        if (existingSession.isPresent()) {
            return existingSession.get();
        }

        // Create new session if none exists
        ChatSession newSession = new ChatSession();
        newSession.setConsumerEmail(consumerEmail);
        newSession.setFarmerEmail(farmerEmail);
        return chatSessionRepo.save(newSession);
    }

    /**
     * Update unread counts for all sessions a user participates in
     */
    private void updateUnreadCountsForSessions(String userEmail) {
        List<ChatSession> sessions = chatSessionRepo.findAllSessionsByUser(userEmail);

        for (ChatSession session : sessions) {
            boolean isConsumer = session.getConsumerEmail().equals(userEmail);
            int unreadCount = chatMessageRepo.countUnreadMessagesInSession(userEmail, session.getId());

            if (isConsumer) {
                session.setConsumerUnreadCount(unreadCount);
            } else {
                session.setFarmerUnreadCount(unreadCount);
            }
        }

        chatSessionRepo.saveAll(sessions);
    }

    /**
     * Check if a user is a participant in a chat session
     */
    private boolean isParticipant(ChatSession session, String email) {
        return session.getConsumerEmail().equals(email) || session.getFarmerEmail().equals(email);
    }

    /**
     * Convert ChatMessage to ChatMessageDTO
     */
    /**
     * Convert ChatMessage to ChatMessageDTO
     */
    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderEmail(message.getSenderEmail());
        dto.setReceiverEmail(message.getReceiverEmail());
        dto.setSenderRole(message.getSenderRole());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead()); // Renamed from 'read' to 'isRead'
        dto.setChatSessionId(message.getChatSession().getId());
        dto.setProductId(message.getProductId());
        return dto;
    }
    /**
     * Convert Farmer to FarmerSummaryDTO
     */
    private FarmerSummaryDTO convertToFarmerSummaryDTO(Farmer farmer) {
        FarmerSummaryDTO dto = new FarmerSummaryDTO();
        dto.setFarmerId(farmer.getFarmerId());
        dto.setFarmerName(farmer.getFarmerName());
        dto.setFarmerEmail(farmer.getFarmerEmail());
        dto.setProfilePhotoPath(farmer.getProfilePhotoPath());
        return dto;
    }

    /**
     * Convert Consumer to ConsumerSummaryDTO
     */
    private ConsumerSummaryDTO convertToConsumerSummaryDTO(Consumer consumer) {
        ConsumerSummaryDTO dto = new ConsumerSummaryDTO();
        dto.setConsumerId(consumer.getConsumerId());
        dto.setConsumerFirstName(consumer.getConsumerFirstName());
        dto.setConsumerLastName(consumer.getConsumerLastName());
        dto.setConsumerEmail(consumer.getConsumerEmail());
        dto.setProfilePhotoPath(consumer.getProfilePhotoPath());
        return dto;
    }

    /**
     * Convert ChatSession to ChatSessionSummaryDTO
     */
    private ChatSessionSummaryDTO convertToSessionSummaryDTO(ChatSession session, String userEmail, String userRole) {
        ChatSessionSummaryDTO dto = new ChatSessionSummaryDTO();
        dto.setId(session.getId());
        dto.setConsumerEmail(session.getConsumerEmail());
        dto.setFarmerEmail(session.getFarmerEmail());
        dto.setLastMessagePreview(session.getLastMessagePreview());
        dto.setUpdatedAt(session.getUpdatedAt());

        // Set unread count based on the user viewing the sessions
        boolean isConsumer = "CONSUMER".equals(userRole);
        dto.setUnreadCount(isConsumer ? session.getConsumerUnreadCount() : session.getFarmerUnreadCount());

        // Load participant details
        try {
            Consumer consumer = consumerRepo.findByConsumerEmail(session.getConsumerEmail());
            if (consumer != null) {
                dto.setConsumerName(consumer.getConsumerFirstName() + " " + consumer.getConsumerLastName());
                dto.setConsumerPhoto(consumer.getProfilePhotoPath());
            }

            Farmer farmer = farmerRepo.findByFarmerEmail(session.getFarmerEmail());
            if (farmer != null) {
                dto.setFarmerName(farmer.getFarmerName());
                dto.setFarmerPhoto(farmer.getProfilePhotoPath());
            }
        } catch (Exception e) {
            logger.warn("Error loading participant details for chat session {}: {}", session.getId(), e.getMessage());
        }

        return dto;
    }

    // Add to ChatService
    @Transactional
    public ApiResponse<ChatSessionSummaryDTO> initiateProductChat(
            int productId, String initialMessage, String consumerEmail) {
        try {
            // Get product to find the farmer
            Product product = productRepo.findById(productId).orElse(null);
            if (product == null) {
                return ApiResponse.error("Product not found", "No product with ID: " + productId);
            }

            // Get farmer's email
            String farmerEmail = product.getFarmer().getFarmerEmail();

            // Get or create chat session
            ChatSession session = getOrCreateChatSession(consumerEmail, farmerEmail, "CONSUMER");

            // Send initial message about the product
            ChatMessage message = ChatMessage.create(
                    initialMessage, consumerEmail, farmerEmail, "CONSUMER", session, productId);
            message = chatMessageRepo.save(message);

            // Update session metadata
            session.updateLastMessagePreview(initialMessage);
            session.setFarmerUnreadCount(session.getFarmerUnreadCount() + 1);
            chatSessionRepo.save(session);

            // Convert to DTO for response
            ChatMessageDTO messageDTO = convertToDTO(message);

            // Publish to Kafka
            kafkaTemplate.send(KafkaConfig.TOPIC_CHAT_MESSAGES, messageDTO);

            // Convert session to summary DTO
            ChatSessionSummaryDTO sessionDTO = convertToSessionSummaryDTO(session, consumerEmail, "CONSUMER");

            return ApiResponse.success("Chat initiated about product #" + productId, sessionDTO);
        } catch (Exception e) {
            logger.error("Failed to initiate product chat", e);
            return ApiResponse.error("Failed to start chat", e.getMessage());
        }
    }
    // Add to ChatService
    public ApiResponse<List<ChatMessageDTO>> getProductRelatedMessages(int productId, String userEmail) {
        try {
            // Find all messages mentioning this product
            List<ChatMessage> messages = chatMessageRepo.findByProductId(productId);

            // Filter to only include messages from conversations this user is part of
            List<ChatMessage> filteredMessages = messages.stream()
                    .filter(msg -> msg.getSenderEmail().equals(userEmail) ||
                            msg.getReceiverEmail().equals(userEmail))
                    .collect(Collectors.toList());

            // Convert to DTOs
            List<ChatMessageDTO> messageDTOs = filteredMessages.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success("Product-related messages retrieved", messageDTOs);
        } catch (Exception e) {
            logger.error("Failed to retrieve product messages", e);
            return ApiResponse.error("Failed to get product messages", e.getMessage());
        }
    }
}
