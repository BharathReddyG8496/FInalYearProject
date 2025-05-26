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
            logger.info("Received message from Kafka: {}", message.getId());
            logger.info("Message content: '{}', From: {}, To: {}",
                    message.getContent(), message.getSenderEmail(), message.getReceiverEmail());

            // Send to user-specific destination
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverEmail(),
                    "/queue/messages",
                    message);

            // Also broadcast to a public topic for debugging
            messagingTemplate.convertAndSend(
                    "/topic/public.messages",
                    message);

            logger.info("Message forwarded to destinations");
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }
}