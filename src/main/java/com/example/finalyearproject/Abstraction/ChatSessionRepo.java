package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepo extends JpaRepository<ChatSession, Long> {

    @Query("SELECT cs FROM ChatSession cs WHERE " +
            "(cs.consumerEmail = :email1 AND cs.farmerEmail = :email2) OR " +
            "(cs.consumerEmail = :email2 AND cs.farmerEmail = :email1)")
    Optional<ChatSession> findByParticipants(@Param("email1") String email1, @Param("email2") String email2);


    @Query("SELECT cs FROM ChatSession cs WHERE cs.consumerEmail = :consumerEmail AND cs.farmerEmail = :farmerEmail")
    Optional<ChatSession> findByConsumerAndFarmer(
            @Param("consumerEmail") String consumerEmail,
            @Param("farmerEmail") String farmerEmail);

    List<ChatSession> findByConsumerEmailOrderByUpdatedAtDesc(String consumerEmail);

    List<ChatSession> findByFarmerEmailOrderByUpdatedAtDesc(String farmerEmail);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.consumerEmail = :email OR cs.farmerEmail = :email " +
            "ORDER BY cs.updatedAt DESC")
    List<ChatSession> findAllSessionsByUser(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE ChatSession cs SET cs.consumerUnreadCount = :count WHERE cs.id = :sessionId")
    void updateConsumerUnreadCount(@Param("sessionId") Long sessionId, @Param("count") int count);

    @Modifying
    @Transactional
    @Query("UPDATE ChatSession cs SET cs.farmerUnreadCount = :count WHERE cs.id = :sessionId")
    void updateFarmerUnreadCount(@Param("sessionId") Long sessionId, @Param("count") int count);

    @Modifying
    @Transactional
    @Query("UPDATE ChatSession cs SET cs.lastMessagePreview = :preview, cs.updatedAt = CURRENT_TIMESTAMP WHERE cs.id = :sessionId")
    void updateLastMessagePreview(@Param("sessionId") Long sessionId, @Param("preview") String preview);
}