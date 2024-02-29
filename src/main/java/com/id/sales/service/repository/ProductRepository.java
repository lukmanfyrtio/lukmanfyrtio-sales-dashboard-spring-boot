package com.id.sales.service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.id.sales.service.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
	List<Product> findByDepartment_Id(UUID departmentId);

	Optional<Product> findByName(String name);
}