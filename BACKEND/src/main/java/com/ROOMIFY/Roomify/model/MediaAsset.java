package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_assets", indexes = {
        @Index(name = "idx_media_path", columnList = "path", unique = true)
})
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String path;

    @Column(nullable = false, length = 128)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] data;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MediaAsset() {}

    public MediaAsset(String path, String contentType, byte[] data) {
        this.path = path;
        this.contentType = contentType != null ? contentType : "application/octet-stream";
        this.data = data;
        this.fileSize = data != null ? (long) data.length : 0L;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.fileSize = data != null ? (long) data.length : 0L;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
