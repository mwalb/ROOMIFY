package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Property;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.repository.PropertyRepository;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "*")
public class PropertyController {

    @Autowired
    private PropertyRepository repository;

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Property>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, repository.findAll(), "Properties retrieved"));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Property>> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(p -> {
                    // Force load units if lazy
                    p.getUnits().size(); 
                    return ResponseEntity.ok(new ApiResponse<>(true, p, "Property found"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Property>> create(@RequestBody Property property) {
        // Link units to property if they exist in request
        if (property.getUnits() != null) {
            for (Room unit : property.getUnits()) {
                unit.setProperty(property);
            }
        }
        Property saved = repository.save(property);
        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Property created"));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<Property>>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, repository.findByOwnerId(ownerId), "Owner properties retrieved"));
    }
}
