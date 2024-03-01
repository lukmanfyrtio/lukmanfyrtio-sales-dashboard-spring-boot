package com.id.sales.service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.id.sales.service.model.Company;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
	Optional<Company> findByName(String name);
}
