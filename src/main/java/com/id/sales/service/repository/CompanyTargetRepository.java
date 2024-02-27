package com.id.sales.service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.id.sales.service.model.CompanyTarget;

public interface CompanyTargetRepository
		extends JpaRepository<CompanyTarget, UUID>, QuerydslPredicateExecutor<CompanyTarget> {
	  Iterable<CompanyTarget> findByDepartment_IdAndYearIsNotNull(UUID departmentId);
}
