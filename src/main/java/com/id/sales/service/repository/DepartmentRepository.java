package com.id.sales.service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.id.sales.service.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, UUID>,QuerydslPredicateExecutor<Department> {

	List<Department> findByNameLikeIgnoreCase(String name);
	long countByIsDisplayTrue();

	// Custom query to get a list of departments with isDisplay set to true
	List<Department> findTop3ByIsDisplayTrue();

	List<Department> findAllByOrderByNameDesc();
}
