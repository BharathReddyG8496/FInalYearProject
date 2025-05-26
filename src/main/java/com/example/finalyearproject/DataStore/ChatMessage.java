package com.example.finalyearproject.DataStore;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private String receiverEmail;

    @Column(nullable = false)
    private String senderRole;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Rename from 'read' to 'isRead'
    @Column(nullable = false)
    private boolean isRead = false;

    private Integer productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id")
    private ChatSession chatSession;

    // Helper methods...
    public static ChatMessage create(String content, String senderEmail, String receiverEmail,
                                     String senderRole, ChatSession session) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setSenderEmail(senderEmail);
        message.setReceiverEmail(receiverEmail);
        message.setSenderRole(senderRole);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        message.setChatSession(session);
        return message;
    }

    public static ChatMessage create(String content, String senderEmail, String receiverEmail,
                                     String senderRole, ChatSession session, Integer productId) {
        ChatMessage message = create(content, senderEmail, receiverEmail, senderRole, session);
        message.setProductId(productId);
        return message;
    }
}