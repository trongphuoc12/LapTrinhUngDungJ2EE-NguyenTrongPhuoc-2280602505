package com.example.Bai5.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Bai5.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
