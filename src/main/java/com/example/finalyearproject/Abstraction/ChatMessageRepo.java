package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatSessionIdOrderByTimestampAsc(Long chatSessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.receiverEmail = :email AND m.isRead = false")
    List<ChatMessage> findUnreadMessagesByReceiver(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.id IN :ids")
    void markMessagesAsRead(@Param("ids") List<Long> messageIds);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiverEmail = :email AND m.chatSession.id = :sessionId")
    int markAllSessionMessagesAsRead(@Param("email") String email, @Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverEmail = :email AND m.isRead = false")
    int countUnreadMessagesByReceiver(@Param("email") String email);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverEmail = :email AND m.chatSession.id = :sessionId AND m.isRead = false")
    int countUnreadMessagesInSession(@Param("email") String email, @Param("sessionId") Long sessionId);

    // Add to ChatMessageRepo
    List<ChatMessage> findByProductId(Integer productId);
}