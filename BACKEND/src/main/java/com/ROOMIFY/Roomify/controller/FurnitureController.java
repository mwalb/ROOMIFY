package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Furniture;
import com.ROOMIFY.Roomify.repository.FurnitureRepository;
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
@RequestMapping("/api/furniture")
@CrossOrigin(origins = "*")
public class FurnitureController {

    @Autowired
    private FurnitureRepository repository;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Furniture>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, repository.findAll(), "Furniture retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Furniture>> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(furniture -> ResponseEntity.ok(new ApiResponse<>(true, furniture, "Furniture found")))
                .orElse(ResponseEntity.status(404).body(new ApiResponse<>(false, null, "Furniture not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Furniture>> create(@RequestBody Furniture furniture) {
        if (furniture.getStatus() == null || furniture.getStatus().isBlank()) {
            furniture.setStatus("AVAILABLE");
        }
        Furniture saved = repository.save(furniture);
        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Furniture created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Furniture>> update(@PathVariable Long id, @RequestBody Furniture updated) {
        return repository.findById(id)
                .map(existing -> {
                    if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
                    if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
                    if (updated.getPrice() > 0) existing.setPrice(updated.getPrice());
                    if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
                    if (updated.getConditionStatus() != null) existing.setConditionStatus(updated.getConditionStatus());
                    if (updated.getOwnerName() != null) existing.setOwnerName(updated.getOwnerName());
                    if (updated.getContactPhone() != null) existing.setContactPhone(updated.getContactPhone());
                    if (updated.getContactEmail() != null) existing.setContactEmail(updated.getContactEmail());
                    if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
                    if (updated.getLatitude() != 0.0) existing.setLatitude(updated.getLatitude());
                    if (updated.getLongitude() != 0.0) existing.setLongitude(updated.getLongitude());
                    if (updated.getShopId() != null) existing.setShopId(updated.getShopId());
                    if (updated.getStatus() != null) existing.setStatus(updated.getStatus());

                    Furniture saved = repository.save(existing);
                    return ResponseEntity.ok(new ApiResponse<>(true, saved, "Furniture updated"));
                })
                .orElse(ResponseEntity.status(404).body(new ApiResponse<>(false, null, "Furniture not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Furniture deleted"));
        }
        return ResponseEntity.status(404).body(new ApiResponse<>(false, null, "Furniture not found"));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse<List<Furniture>>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(new ApiResponse<>(true, repository.findByShopId(shopId), "Shop furniture retrieved"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Furniture>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new ApiResponse<>(true, repository.findByPostedBy(userId), "User furniture retrieved"));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] files) {
        try {
            Furniture furniture = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Furniture not found"));

            List<String> imageUrls = new ArrayList<>();
            Path uploadPath = Paths.get(uploadDir, "furniture", String.valueOf(id));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID().toString() + ".jpg";
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, file.getBytes());
                imageUrls.add("/uploads/furniture/" + id + "/" + fileName);
            }

            furniture.getImages().addAll(imageUrls);
            repository.save(furniture);

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
            Furniture furniture = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Furniture not found"));

            Path uploadPath = Paths.get(uploadDir, "furniture", String.valueOf(id));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = "video_" + UUID.randomUUID().toString() + ".mp4";
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            String videoUrl = "/uploads/furniture/" + id + "/" + fileName;
            furniture.setVideoUrl(videoUrl);
            furniture.setHasVideo(true);
            repository.save(furniture);

            return ResponseEntity.ok(new ApiResponse<>(true, videoUrl, "Video uploaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}
