package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.dto.PropertyResponseDTO;
import com.ROOMIFY.Roomify.model.Property;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.PropertyRepository;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private PropertyRepository repository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private List<PropertyResponseDTO> toDTOList(List<Property> properties) {
        if (properties.isEmpty()) return Collections.emptyList();

        Set<Long> ownerIds = properties.stream()
                .map(Property::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> ownerMap = ownerIds.isEmpty() ? Collections.emptyMap() :
                userRepository.findAllById(ownerIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        return properties.stream()
                .map(p -> PropertyResponseDTO.fromEntity(p, ownerMap.get(p.getOwnerId())))
                .collect(Collectors.toList());
    }

    private PropertyResponseDTO toDTO(Property p) {
        User owner = p.getOwnerId() != null ? userRepository.findById(p.getOwnerId()).orElse(null) : null;
        return PropertyResponseDTO.fromEntity(p, owner);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponseDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, toDTOList(repository.findAll()), "Properties retrieved"));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PropertyResponseDTO>> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(p -> {
                    // Force load units if lazy
                    p.getUnits().size(); 
                    return ResponseEntity.ok(new ApiResponse<>(true, toDTO(p), "Property found"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<PropertyResponseDTO>> create(@RequestBody Property property) {
        // Synchronize owner details from User entity once (single DB lookup)
        User owner = null;
        if (property.getOwnerId() != null) {
            owner = userRepository.findById(property.getOwnerId()).orElse(null);
            if (owner != null) {
                property.setOwnerName(owner.getName());
                if (property.getContactPhone() == null || property.getContactPhone().isBlank()) {
                    property.setContactPhone(owner.getPhone());
                }
                if (property.getContactEmail() == null || property.getContactEmail().isBlank()) {
                    property.setContactEmail(owner.getEmail());
                }
            }
        }

        // Obey 3NF: Link units to property if they exist in request
        if (property.getUnits() != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (Room unit : property.getUnits()) {
                unit.setProperty(property);
                if (unit.getPostedBy() == null || unit.getPostedBy() == 0L) {
                    unit.setPostedBy(property.getOwnerId());
                }
                if (unit.getCreatedAt() == null) unit.setCreatedAt(now);
                if (unit.getUpdatedAt() == null) unit.setUpdatedAt(now);
            }
        }

        Property saved = repository.save(property);
        return ResponseEntity.ok(new ApiResponse<>(true, PropertyResponseDTO.fromEntity(saved, owner), "Property created"));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<PropertyResponseDTO>>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, toDTOList(repository.findByOwnerId(ownerId)), "Owner properties retrieved"));
    }
}
