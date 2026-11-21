package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.dto.FurnitureResponseDTO;
import com.ROOMIFY.Roomify.model.Furniture;
import com.ROOMIFY.Roomify.model.Shop;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.FurnitureRepository;
import com.ROOMIFY.Roomify.repository.ShopRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/furniture")
public class FurnitureController {

    @Autowired
    private FurnitureRepository repository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.ROOMIFY.Roomify.service.MediaStorageService mediaStorageService;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    private FurnitureResponseDTO toDTO(Furniture f) {
        Shop shop = (f.getShopId() != null) ? shopRepository.findById(f.getShopId()).orElse(null) : null;
        User user = (f.getPostedBy() != null) ? userRepository.findById(f.getPostedBy()).orElse(null) : null;
        return FurnitureResponseDTO.fromEntity(f, shop, user);
    }

    private List<FurnitureResponseDTO> toDTOList(List<Furniture> list) {
        if (list.isEmpty()) return Collections.emptyList();

        Set<Long> shopIds = list.stream()
                .map(Furniture::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> userIds = list.stream()
                .map(Furniture::getPostedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Shop> shopMap = shopIds.isEmpty() ? Collections.emptyMap() :
                shopRepository.findAllById(shopIds).stream()
                        .collect(Collectors.toMap(Shop::getId, Function.identity()));

        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        return list.stream()
                .map(f -> {
                    Shop shop = f.getShopId() != null ? shopMap.get(f.getShopId()) : null;
                    User user = f.getPostedBy() != null ? userMap.get(f.getPostedBy()) : null;
                    return FurnitureResponseDTO.fromEntity(f, shop, user);
                })
                .collect(Collectors.toList());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FurnitureResponseDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, toDTOList(repository.findAll()), "Furniture retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FurnitureResponseDTO>> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(furniture -> ResponseEntity.ok(new ApiResponse<>(true, toDTO(furniture), "Furniture found")))
                .orElse(ResponseEntity.status(404).body(new ApiResponse<>(false, null, "Furniture not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FurnitureResponseDTO>> create(@RequestBody Furniture furniture) {
        if (furniture.getStatus() == null || furniture.getStatus().isBlank()) {
            furniture.setStatus("AVAILABLE");
        }

        // In 3NF: If associated with a Shop, synchronize or clear redundant contact details
        // while preserving data consistency
        if (furniture.getShopId() != null) {
            shopRepository.findById(furniture.getShopId()).ifPresent(shop -> {
                furniture.setOwnerName(shop.getName());
                furniture.setContactPhone(shop.getContactPhone());
                furniture.setContactEmail(shop.getContactEmail());
                furniture.setAddress(shop.getAddress());
                furniture.setLatitude(shop.getLatitude());
                furniture.setLongitude(shop.getLongitude());
            });
        } else if (furniture.getPostedBy() != null) {
            userRepository.findById(furniture.getPostedBy()).ifPresent(user -> {
                if (furniture.getOwnerName() == null || furniture.getOwnerName().isBlank()) {
                    furniture.setOwnerName(user.getName());
                }
                if (furniture.getContactPhone() == null || furniture.getContactPhone().isBlank()) {
                    furniture.setContactPhone(user.getPhone());
                }
                if (furniture.getContactEmail() == null || furniture.getContactEmail().isBlank()) {
                    furniture.setContactEmail(user.getEmail());
                }
            });
        }

        Furniture saved = repository.save(furniture);
        return ResponseEntity.ok(new ApiResponse<>(true, toDTO(saved), "Furniture created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FurnitureResponseDTO>> update(@PathVariable Long id, @RequestBody Furniture updated) {
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

                    // If shop changed, update references
                    if (existing.getShopId() != null) {
                        shopRepository.findById(existing.getShopId()).ifPresent(shop -> {
                            existing.setOwnerName(shop.getName());
                            existing.setContactPhone(shop.getContactPhone());
                            existing.setContactEmail(shop.getContactEmail());
                            existing.setAddress(shop.getAddress());
                            existing.setLatitude(shop.getLatitude());
                            existing.setLongitude(shop.getLongitude());
                        });
                    }

                    Furniture saved = repository.save(existing);
                    return ResponseEntity.ok(new ApiResponse<>(true, toDTO(saved), "Furniture updated"));
                })
                .orElse(ResponseEntity.status(404).bRoom managedRoomody(new ApiResponse<>(false, null, "Furniture not found")));
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
    public ResponseEntity<ApiResponse<List<FurnitureResponseDTO>>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(new ApiResponse<>(true, toDTOList(repository.findByShopId(shopId)), "Shop furniture retrieved"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<FurnitureResponseDTO>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new ApiResponse<>(true, toDTOList(repository.findByPostedBy(userId)), "User furniture retrieved"));
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
            Furniture furniture = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Furniture not found"));

            List<String> imageUrls = new ArrayList<>();
            Path uploadPath = getUploadBasePath().resolve("furniture").resolve(String.valueOf(id));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID().toString() + ".jpg";
                String fileUrl = "/uploads/furniture/" + id + "/" + fileName;
                mediaStorageService.storeMedia(fileUrl, file.getContentType(), file.getBytes());
                imageUrls.add(fileUrl);
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

            String fileName = "video_" + UUID.randomUUID().toString() + ".mp4";
            String videoUrl = "/uploads/furniture/" + id + "/" + fileName;
            mediaStorageService.storeMedia(videoUrl, file.getContentType(), file.getBytes());
            furniture.setVideoUrl(videoUrl);
            furniture.setHasVideo(true);
            repository.save(furniture);

            return ResponseEntity.ok(new ApiResponse<>(true, videoUrl, "Video uploaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}
