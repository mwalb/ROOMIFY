package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;
    private Long receiverId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private LocalDateTime timestamp;
    private boolean isRead;
    
    // The property context for the chat
    private Long roomId;
    private String roomTitle;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    public ChatMessage(Long senderId, Long receiverId, String content, Long roomId, String roomTitle) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.roomId = roomId;
        this.roomTitle = roomTitle;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }
}
