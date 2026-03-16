package com.example.Bai6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Bai6.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
