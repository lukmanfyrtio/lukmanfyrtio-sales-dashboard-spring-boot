package com.id.sales.service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.id.sales.service.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    // Add custom queries if needed
}