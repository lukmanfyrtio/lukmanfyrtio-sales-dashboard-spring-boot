package com.id.sales.service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.id.sales.service.model.UserMapping;

public interface UserMappingRepository extends JpaRepository<UserMapping, UUID> {
    // Add custom queries if needed
}