package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    Optional<MediaAsset> findByPath(String path);
    boolean existsByPath(String path);
}
