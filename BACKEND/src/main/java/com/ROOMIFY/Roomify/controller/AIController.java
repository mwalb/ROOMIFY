package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.dto.ai.AIRoomRequest;
import com.ROOMIFY.Roomify.dto.ai.AIRoomResponse;
import com.ROOMIFY.Roomify.model.MediaAsset;
import com.ROOMIFY.Roomify.service.AIService;
import com.ROOMIFY.Roomify.service.MediaStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private MediaStorageService mediaStorageService;

    @PostMapping("/visualize")
    public ResponseEntity<ApiResponse<AIRoomResponse>> visualizeRoom(@RequestBody AIRoomRequest request) {
        try {
            byte[] imageBytes = null;
            String sourceUrl = request.getSourceImageUrl();

            if (sourceUrl != null && !sourceUrl.isBlank()) {
                int uploadsIdx = sourceUrl.indexOf("/uploads/");
                String normalizedPath = uploadsIdx >= 0 ? sourceUrl.substring(uploadsIdx) : sourceUrl;
                Optional<MediaAsset> asset = mediaStorageService.findMedia(normalizedPath);
                if (asset.isPresent()) {
                    imageBytes = asset.get().getData();
                }
            }

            AIRoomResponse response = aiService.generateRoomArrangement(request, imageBytes);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Room visualization generated successfully"));
        } catch (Exception e) {
            String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "AI Generation failed. Please try again.";
            return ResponseEntity.status(500).body(new ApiResponse<>(false, null, msg));
        }
    }

    @PostMapping("/visualize-custom")
    public ResponseEntity<ApiResponse<AIRoomResponse>> visualizeCustomRoom(
            @RequestPart("request") AIRoomRequest request,
            @RequestPart("image") MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            AIRoomResponse response = aiService.generateRoomArrangement(request, imageBytes);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Custom room visualization generated successfully"));
        } catch (Exception e) {
            String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "AI Generation failed. Please try again.";
            return ResponseEntity.status(500).body(new ApiResponse<>(false, null, msg));
        }
    }
}
