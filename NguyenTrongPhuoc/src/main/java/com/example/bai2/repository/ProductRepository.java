package com.example.bai2.repository;

import com.example.bai2.model.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<Product> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryIdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(Long categoryId, String keyword);

    long countByActiveTrue();
}
