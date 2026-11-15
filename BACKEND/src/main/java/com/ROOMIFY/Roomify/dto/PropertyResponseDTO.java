package com.ROOMIFY.Roomify.dto;

import com.ROOMIFY.Roomify.model.Property;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PropertyResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private Long ownerId;
    private String ownerName;
    private String contactPhone;
    private String contactEmail;
    private String propertyType;
    private boolean hasVideo;
    private boolean hasContract;
    private String videoUrl;
    private String contractUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Room> units = new ArrayList<>();
    private List<String> images = new ArrayList<>();

    public PropertyResponseDTO() {}

    public static PropertyResponseDTO fromEntity(Property p, User owner) {
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setAddress(p.getAddress());
        dto.setLatitude(p.getLatitude());
        dto.setLongitude(p.getLongitude());
        dto.setOwnerId(p.getOwnerId());
        dto.setPropertyType(p.getPropertyType());
        dto.setHasVideo(p.isHasVideo());
        dto.setHasContract(p.isHasContract());
        dto.setVideoUrl(p.getVideoUrl());
        dto.setContractUrl(p.getContractUrl());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        dto.setUnits(p.getUnits());
        dto.setImages(p.getImages() != null ? new ArrayList<>(p.getImages()) : new ArrayList<>());

        // Obey 3NF: Resolve landlord contact dynamically from User table
        if (owner != null) {
            dto.setOwnerName(owner.getName());
            dto.setContactPhone(owner.getPhone());
            dto.setContactEmail(owner.getEmail());
        } else {
            dto.setOwnerName(p.getOwnerName());
            dto.setContactPhone(p.getContactPhone());
            dto.setContactEmail(p.getContactEmail());
        }

        return dto;
    }
}
