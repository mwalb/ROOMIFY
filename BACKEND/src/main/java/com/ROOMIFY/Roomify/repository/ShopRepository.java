package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByOwnerId(Long ownerId);
    List<Shop> findByNameContainingIgnoreCase(String name);
}
