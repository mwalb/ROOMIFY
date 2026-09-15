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

    @PostMapping
    public ResponseEntity<ApiResponse<Furniture>> create(@RequestBody Furniture furniture) {
        Furniture saved = repository.save(furniture);
        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Furniture created"));
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
}
