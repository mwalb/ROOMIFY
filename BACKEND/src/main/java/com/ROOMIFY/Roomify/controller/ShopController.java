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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    @Autowired
    private ShopRepository repository;

    @Autowired
    private com.ROOMIFY.Roomify.service.MediaStorageService mediaStorageService;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Shop>>> getAll() {
        List<Shop> all = repository.findAll();
        Map<String, Shop> unique = new LinkedHashMap<>();
        for (Shop s : all) {
            if (s.getId() != null) {
                String normName = s.getName() != null ? s.getName().trim().toLowerCase() : "";
                String normPhone = s.getContactPhone() != null ? s.getContactPhone().trim().replaceAll("[^0-9]", "") : "";
                String key = normName.isEmpty() ? ("id_" + s.getId()) : (normName + "_" + normPhone);
                unique.putIfAbsent(key, s);
            }
        }
        return ResponseEntity.ok(new ApiResponse<>(true, new ArrayList<>(unique.values()), "Shops retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Shop>> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(s -> ResponseEntity.ok(new ApiResponse<>(true, s, "Shop found")))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Shop>> create(@RequestBody Shop shop) {
        if (shop.getName() != null && !shop.getName().trim().isEmpty()) {
            String incomingName = shop.getName().trim().toLowerCase();
            String incomingPhone = shop.getContactPhone() != null ? shop.getContactPhone().trim().replaceAll("[^0-9]", "") : "";

            List<Shop> allShops = repository.findAll();
            for (Shop existing : allShops) {
                String existingName = existing.getName() != null ? existing.getName().trim().toLowerCase() : "";
                String existingPhone = existing.getContactPhone() != null ? existing.getContactPhone().trim().replaceAll("[^0-9]", "") : "";

                boolean nameMatches = incomingName.equals(existingName);
                boolean phoneMatches = !incomingPhone.isEmpty() && incomingPhone.equals(existingPhone);
                boolean ownerMatches = shop.getOwnerId() != null && shop.getOwnerId().equals(existing.getOwnerId());

                if (nameMatches && (phoneMatches || ownerMatches || incomingPhone.isEmpty())) {
                    // Update existing shop to prevent duplicate entries
                    existing.setName(shop.getName());
                    if (shop.getDescription() != null) existing.setDescription(shop.getDescription());
                    if (shop.getAddress() != null) existing.setAddress(shop.getAddress());
                    if (shop.getContactPhone() != null) existing.setContactPhone(shop.getContactPhone());
                    if (shop.getContactEmail() != null) existing.setContactEmail(shop.getContactEmail());
                    if (shop.getWebsiteUrl() != null) existing.setWebsiteUrl(shop.getWebsiteUrl());
                    if (shop.getLatitude() != 0.0) existing.setLatitude(shop.getLatitude());
                    if (shop.getLongitude() != 0.0) existing.setLongitude(shop.getLongitude());
                    if (shop.getLogoUrl() != null) existing.setLogoUrl(shop.getLogoUrl());
                    if (shop.getVideoUrl() != null) existing.setVideoUrl(shop.getVideoUrl());
                    if (shop.getImages() != null && !shop.getImages().isEmpty()) {
                        for (String img : shop.getImages()) {
                            if (!existing.getImages().contains(img)) {
                                existing.getImages().add(img);
                            }
                        }
                    }
                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                    Shop updated = repository.save(existing);
                    return ResponseEntity.ok(new ApiResponse<>(true, updated, "Shop updated"));
                }
            }
        }
        Shop saved = repository.save(shop);
        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Shop created"));
    }

    private Path getUploadBasePath() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(path) && Files.exists(Paths.get("BACKEND", uploadDir))) {
            return Paths.get("BACKEND", uploadDir).toAbsolutePath().normalize();
        }
        return path;
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] files) {
        try {
            Shop shop = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Shop not found"));

            List<String> imageUrls = new ArrayList<>();
            Path uploadPath = getUploadBasePath().resolve("shops").resolve(String.valueOf(id));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID().toString() + ".jpg";
                String fileUrl = "/uploads/shops/" + id + "/" + fileName;
                mediaStorageService.storeMedia(fileUrl, file.getContentType(), file.getBytes());
                imageUrls.add(fileUrl);
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

            String fileName = UUID.randomUUID().toString() + ".mp4";
            String videoUrl = "/uploads/shops/" + id + "/videos/" + fileName;
            mediaStorageService.storeMedia(videoUrl, file.getContentType(), file.getBytes());
            shop.setVideoUrl(videoUrl);
            shop.setHasVideo(true);
            repository.save(shop);

            return ResponseEntity.ok(new ApiResponse<>(true, videoUrl, "Video uploaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}
