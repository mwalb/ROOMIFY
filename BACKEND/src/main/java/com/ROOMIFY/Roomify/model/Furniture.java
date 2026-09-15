package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "furniture")
@Getter
@Setter
public class Furniture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private double price;
    private String category;
    private String conditionStatus; // NEW, USED
    
    private Long postedBy;
    private String ownerName;
    private String contactPhone;
    private String contactEmail;
    
    private double latitude;
    private double longitude;
    private String address;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "furniture_images", joinColumns = @JoinColumn(name = "furniture_id"))
    @Column(name = "image")
    private List<String> images = new ArrayList<>();
    
    private String status = "AVAILABLE"; // AVAILABLE, SOLD
    
    private LocalDateTime createdAt = LocalDateTime.now();

    public Furniture() {}
}
