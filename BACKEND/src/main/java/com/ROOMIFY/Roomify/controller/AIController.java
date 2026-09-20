package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.dto.ai.AIRoomRequest;
import com.ROOMIFY.Roomify.dto.ai.AIRoomResponse;
import com.ROOMIFY.Roomify.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/visualize")
    public ResponseEntity<ApiResponse<AIRoomResponse>> visualizeRoom(@RequestBody AIRoomRequest request) {
        try {
            // If the request has a sourceImageUrl that points to a local file
            byte[] imageBytes;
            if (request.getSourceImageUrl().startsWith("/uploads/")) {
                String relativePath = request.getSourceImageUrl().substring("/uploads/".length());
                Path path = Paths.get(uploadDir, relativePath);
                imageBytes = Files.readAllBytes(path);
            } else {
                // Fetch from remote URL if needed, but for Roomify we usually have local uploads
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Invalid image URL. Must be a local Roomify upload."));
            }

            AIRoomResponse response = aiService.generateRoomArrangement(request, imageBytes);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Room visualization generated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, null, "AI Generation failed: " + e.getMessage()));
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
            return ResponseEntity.status(500).body(new ApiResponse<>(false, null, "AI Generation failed: " + e.getMessage()));
        }
    }
}
