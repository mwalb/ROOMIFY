package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.ChatMessage;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.ChatMessageRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(@RequestBody ChatMessage message) {
        try {
            message.setTimestamp(LocalDateTime.now());
            message.setRead(false);
            ChatMessage saved = chatRepository.save(message);
            return ResponseEntity.ok(new ApiResponse<>(true, saved, "Message sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getHistory(
            @RequestParam Long user1,
            @RequestParam Long user2,
            @RequestParam(required = false) Long roomId) {
        
        List<ChatMessage> history;
        if (roomId != null) {
            history = chatRepository.findConversationForRoom(user1, user2, roomId);
        } else {
            history = chatRepository.findConversation(user1, user2);
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, history, "History retrieved"));
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getConversations(@PathVariable Long userId) {
        List<ChatMessage> latest = chatRepository.findLatestMessagesGrouped(userId);
        
        List<Map<String, Object>> result = latest.stream().map(msg -> {
            Long otherId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            User otherUser = userRepository.findById(otherId).orElse(null);
            
            java.util.Map<String, Object> convMap = new java.util.HashMap<>();
            convMap.put("conversationId", msg.getId());
            convMap.put("otherPartyId", otherId);
            convMap.put("otherPartyName", otherUser != null ? otherUser.getName() : "Unknown User");
            convMap.put("otherPartyRole", otherUser != null ? otherUser.getRole().toString() : "UNKNOWN");
            convMap.put("lastMessage", msg.getContent());
            convMap.put("lastMessageTime", msg.getTimestamp().toString());
            convMap.put("roomId", msg.getRoomId() != null ? msg.getRoomId() : 0L);
            convMap.put("roomTitle", msg.getRoomTitle() != null ? msg.getRoomTitle() : "Property Inquiry");
            
            return convMap;
        }).collect(Collectors.toList());

        
        return ResponseEntity.ok(new ApiResponse<>(true, result, "Conversations retrieved"));
    }
}
