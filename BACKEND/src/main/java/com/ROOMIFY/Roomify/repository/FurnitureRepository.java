package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.Furniture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FurnitureRepository extends JpaRepository<Furniture, Long> {
    List<Furniture> findByStatus(String status);
    List<Furniture> findByCategory(String category);
}
