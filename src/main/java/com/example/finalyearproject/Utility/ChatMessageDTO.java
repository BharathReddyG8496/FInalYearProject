package com.example.finalyearproject.Utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String content;
    private String senderEmail;
    private String receiverEmail;
    private String senderRole;
    private LocalDateTime timestamp;
    private boolean isRead; // Changed from 'read' to 'isRead'
    private Long chatSessionId;
    private Integer productId;
}