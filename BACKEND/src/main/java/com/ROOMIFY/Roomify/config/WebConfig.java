package com.ROOMIFY.Roomify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void initUploadDirectory() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(path) && Files.exists(Paths.get("BACKEND", uploadDir))) {
            path = Paths.get("BACKEND", uploadDir).toAbsolutePath().normalize();
        }
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception ignored) {}
    }
}
