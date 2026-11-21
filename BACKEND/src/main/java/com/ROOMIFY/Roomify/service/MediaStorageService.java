package com.ROOMIFY.Roomify.service;

import com.ROOMIFY.Roomify.model.MediaAsset;
import com.ROOMIFY.Roomify.repository.MediaAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class MediaStorageService {

    @Autowired
    private MediaAssetRepository repository;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    public Path getUploadBasePath() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(path) && Files.exists(Paths.get("BACKEND", uploadDir))) {
            return Paths.get("BACKEND", uploadDir).toAbsolutePath().normalize();
        }
        return path;
    }

    /**
     * Persists a media asset both on disk (for local fast access) and in the database
     * (so it survives container restarts and redeployments on ephemeral hosts like Fly.io).
     */
    @Transactional
    public String storeMedia(String urlPath, String contentType, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return urlPath;
        }
        String normalizedPath = urlPath.startsWith("/") ? urlPath : ("/" + urlPath);
        String resolvedContentType = resolveContentType(normalizedPath, contentType);

        // 1. Write to local disk as primary cache
        try {
            String rel = normalizedPath.startsWith("/uploads/")
                    ? normalizedPath.substring("/uploads/".length())
                    : (normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath);
            Path diskPath = getUploadBasePath().resolve(rel).normalize();
            if (diskPath.getParent() != null && !Files.exists(diskPath.getParent())) {
                Files.createDirectories(diskPath.getParent());
            }
            Files.write(diskPath, bytes);
        } catch (Exception e) {
            System.err.println("MediaStorageService: Disk write warning for " + normalizedPath + ": " + e.getMessage());
        }

        // 2. Persist in database for permanent cloud durability
        try {
            MediaAsset asset = repository.findByPath(normalizedPath).orElseGet(MediaAsset::new);
            asset.setPath(normalizedPath);
            asset.setContentType(resolvedContentType);
            asset.setData(bytes);
            repository.save(asset);
        } catch (Exception e) {
            System.err.println("MediaStorageService: DB persist warning for " + normalizedPath + ": " + e.getMessage());
        }

        return normalizedPath;
    }

    @Transactional(readOnly = true)
    public Optional<MediaAsset> findMedia(String urlPath) {
        String normalizedPath = urlPath.startsWith("/") ? urlPath : ("/" + urlPath);

        // 1. Check disk first
        try {
            String rel = normalizedPath.startsWith("/uploads/")
                    ? normalizedPath.substring("/uploads/".length())
                    : (normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath);
            Path diskPath = getUploadBasePath().resolve(rel).normalize();
            if (Files.exists(diskPath) && Files.isRegularFile(diskPath)) {
                byte[] bytes = Files.readAllBytes(diskPath);
                String contentType = resolveContentType(normalizedPath, Files.probeContentType(diskPath));
                return Optional.of(new MediaAsset(normalizedPath, contentType, bytes));
            }
        } catch (Exception ignored) {}

        // 2. Check database
        Optional<MediaAsset> dbAsset = repository.findByPath(normalizedPath);
        if (dbAsset.isPresent()) {
            MediaAsset asset = dbAsset.get();
            // Restore back to local disk cache so future reads are instant
            try {
                String rel = normalizedPath.startsWith("/uploads/")
                        ? normalizedPath.substring("/uploads/".length())
                        : (normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath);
                Path diskPath = getUploadBasePath().resolve(rel).normalize();
                if (diskPath.getParent() != null && !Files.exists(diskPath.getParent())) {
                    Files.createDirectories(diskPath.getParent());
                }
                Files.write(diskPath, asset.getData());
            } catch (Exception ignored) {}
            return Optional.of(asset);
        }

        return Optional.empty();
    }

    public static String resolveContentType(String path, String providedType) {
        if (providedType != null && !providedType.isBlank() && !providedType.equals("application/octet-stream")) {
            return providedType;
        }
        String lower = path.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
