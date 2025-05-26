package com.example.finalyearproject.DataStore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"consumer_email", "farmer_email"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_email", nullable = false)
    private String consumerEmail;

    @Column(name = "farmer_email", nullable = false)
    private String farmerEmail;

    // To show last message preview
    @Column(length = 255)
    private String lastMessagePreview;

    // Count of unread messages for consumer
    private int consumerUnreadCount = 0;

    // Count of unread messages for farmer
    private int farmerUnreadCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to update last message preview
    public void updateLastMessagePreview(String content) {
        // Truncate if too long
        if (content != null) {
            if (content.length() > 50) {
                this.lastMessagePreview = content.substring(0, 47) + "...";
            } else {
                this.lastMessagePreview = content;
            }
        }
    }
}