package com.ROOMIFY.Roomify.dto;

import com.ROOMIFY.Roomify.model.Furniture;
import com.ROOMIFY.Roomify.model.Shop;
import com.ROOMIFY.Roomify.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FurnitureResponseDTO {

    private Long id;
    private String title;
    private String description;
    private double price;
    private String category;
    private String conditionStatus;

    private Long postedBy;
    private String ownerName;
    private String contactPhone;
    private String contactEmail;

    private double latitude;
    private double longitude;
    private String address;

    private List<String> images = new ArrayList<>();
    private Long shopId;
    private String videoUrl;
    private boolean hasVideo;
    private String status;
    private LocalDateTime createdAt;

    public FurnitureResponseDTO() {}

    public static FurnitureResponseDTO fromEntity(Furniture f, Shop shop, User user) {
        FurnitureResponseDTO dto = new FurnitureResponseDTO();
        dto.setId(f.getId());
        dto.setTitle(f.getTitle());
        dto.setDescription(f.getDescription());
        dto.setPrice(f.getPrice());
        dto.setCategory(f.getCategory());
        dto.setConditionStatus(f.getConditionStatus());
        dto.setPostedBy(f.getPostedBy());
        dto.setShopId(f.getShopId());
        dto.setVideoUrl(f.getVideoUrl());
        dto.setHasVideo(f.isHasVideo());
        dto.setStatus(f.getStatus());
        dto.setCreatedAt(f.getCreatedAt());

        if (f.getImages() != null) {
            dto.setImages(new ArrayList<>(f.getImages()));
        }

        // Priority 1: If linked to a Shop, dynamically resolve from Shop
        if (shop != null) {
            dto.setOwnerName(shop.getName());
            dto.setContactPhone(shop.getContactPhone());
            dto.setContactEmail(shop.getContactEmail());
            dto.setAddress(shop.getAddress());
            dto.setLatitude(shop.getLatitude());
            dto.setLongitude(shop.getLongitude());
        } else {
            // Priority 2: Independent seller - resolve contact from User
            if (user != null) {
                dto.setOwnerName(user.getName());
                dto.setContactPhone(user.getPhone());
                dto.setContactEmail(user.getEmail());
            } else {
                dto.setOwnerName(f.getOwnerName());
                dto.setContactPhone(f.getContactPhone());
                dto.setContactEmail(f.getContactEmail());
            }
            // Use specific furniture location if posted, otherwise fallback to user
            dto.setAddress(f.getAddress());
            dto.setLatitude(f.getLatitude());
            dto.setLongitude(f.getLongitude());
        }

        return dto;
    }
}
