package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Shop;
import com.ROOMIFY.Roomify.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin(origins = "*")
public class ShopController {

    @Autowired
    private ShopRepository repository;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Shop>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, repository.findAll(), "Shops retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Shop>> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(s -> ResponseEntity.ok(new ApiResponse<>(true, s, "Shop found")))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Shop>> create(@RequestBody Shop shop) {
        Shop saved = repository.save(shop);
        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Shop created"));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] files) {
        try {
            Shop shop = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Shop not found"));

            List<String> imageUrls = new ArrayList<>();
            Path uploadPath = Paths.get(uploadDir, "shops", String.valueOf(id));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID().toString() + ".jpg";
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, file.getBytes());
                imageUrls.add("/uploads/shops/" + id + "/" + fileName);
            }

            shop.getImages().addAll(imageUrls);
            repository.save(shop);

            return ResponseEntity.ok(new ApiResponse<>(true, imageUrls, "Images uploaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/video")
    public ResponseEntity<ApiResponse<String>> uploadVideo(
            @PathVariable Long id,
            @RequestParam("video") MultipartFile file) {
        try {
            Shop shop = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Shop not found"));

            Path uploadPath = Paths.get(uploadDir, "shops", String.valueOf(id), "videos");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID().toString() + ".mp4";
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            String videoUrl = "/uploads/shops/" + id + "/videos/" + fileName;
            shop.setVideoUrl(videoUrl);
            shop.setHasVideo(true);
            repository.save(shop);

            return ResponseEntity.ok(new ApiResponse<>(true, videoUrl, "Video uploaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}
