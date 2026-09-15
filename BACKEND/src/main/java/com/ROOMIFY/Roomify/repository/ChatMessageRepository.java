package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :u1 AND m.receiverId = :u2) OR (m.senderId = :u2 AND m.receiverId = :u1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversation(@Param("u1") Long userId1, @Param("u2") Long userId2);

    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :u1 AND m.receiverId = :u2 AND m.roomId = :roomId) OR (m.senderId = :u2 AND m.receiverId = :u1 AND m.roomId = :roomId) ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversationForRoom(@Param("u1") Long userId1, @Param("u2") Long userId2, @Param("roomId") Long roomId);

    List<ChatMessage> findByReceiverIdAndIsReadFalse(Long receiverId);

    @Query("SELECT m FROM ChatMessage m WHERE m.id IN (SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.senderId = :userId OR m2.receiverId = :userId GROUP BY CASE WHEN m2.senderId = :userId THEN m2.receiverId ELSE m2.senderId END, m2.roomId) ORDER BY m.timestamp DESC")
    List<ChatMessage> findLatestMessagesGrouped(@Param("userId") Long userId);
}
