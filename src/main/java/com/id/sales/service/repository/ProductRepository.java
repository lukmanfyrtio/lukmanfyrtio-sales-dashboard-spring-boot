package com.id.sales.service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.id.sales.service.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> ,QuerydslPredicateExecutor<Product>{
	List<Product> findByDepartment_Id(UUID departmentId);

	Optional<Product> findByName(String name);
}