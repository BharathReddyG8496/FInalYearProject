package com.example.finalyearproject.Utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionSummaryDTO {
    private Long id;
    private String consumerEmail;
    private String consumerName;
    private String consumerPhoto;
    private String farmerEmail;
    private String farmerName;
    private String farmerPhoto;
    private String lastMessagePreview;
    private int unreadCount;
    private LocalDateTime updatedAt;
}