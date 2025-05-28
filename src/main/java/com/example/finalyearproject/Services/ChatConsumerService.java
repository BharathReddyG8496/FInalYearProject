package com.example.finalyearproject.Services;

import com.example.finalyearproject.Configs.KafkaConfig;
import com.example.finalyearproject.Utility.ChatMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(ChatConsumerService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaConfig.TOPIC_CHAT_MESSAGES, groupId = "chat-group")
    public void consumeChatMessage(ChatMessageDTO message) {
        try {
            logger.info("Received message from Kafka: ID={}, From={}, To={}",
                    message.getId(), message.getSenderEmail(), message.getReceiverEmail());

            // Also send back to sender for confirmation (optional)
            messagingTemplate.convertAndSendToUser(
                    message.getSenderEmail(),
                    "/queue/messages",
                    message);

            // Send to a session-specific topic for both users
            String sessionTopic = "/topic/chat." + message.getChatSessionId();
            messagingTemplate.convertAndSend(sessionTopic, message);

            logger.info("Message forwarded to WebSocket destinations");

        } catch (Exception e) {
            logger.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }
}
