package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shops")
@Getter
@Setter
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String address;
    private double latitude;
    private double longitude;
    
    private String contactPhone;
    private String contactEmail;
    private String websiteUrl;
    
    @Column(name = "owner_id")
    private Long ownerId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shop_images", joinColumns = @JoinColumn(name = "shop_id"))
    @Column(name = "image")
    private List<String> images = new ArrayList<>();
    
    private String logoUrl;
    private String videoUrl;
    private boolean hasVideo;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Shop() {}
}
