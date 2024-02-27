package com.id.sales.service.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.id.sales.service.model.CompanyTarget;
import com.id.sales.service.service.CompanyTargetService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/company-targets")
@SecurityRequirement(name = "bearerAuth")
public class CompanyTargetController {

	@Autowired
	private CompanyTargetService companyTargetService;

	@GetMapping
	public List<CompanyTarget> getAllCompanyTargets() {
		return companyTargetService.getAllCompanyTargets();
	}

	@GetMapping("/{id}")
	public ResponseEntity<CompanyTarget> getCompanyTargetById(@PathVariable UUID id) {
		Optional<CompanyTarget> companyTarget = companyTargetService.getCompanyTargetById(id);
		return companyTarget.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<CompanyTarget> createCompanyTarget(@RequestBody CompanyTarget companyTarget) {
		CompanyTarget createdCompanyTarget = companyTargetService.createCompanyTarget(companyTarget);
		return ResponseEntity.ok(createdCompanyTarget);
	}

	@PutMapping("/{id}")
	public ResponseEntity<CompanyTarget> updateCompanyTarget(@PathVariable UUID id,
			@RequestBody CompanyTarget updatedCompanyTarget) {
		CompanyTarget companyTarget = companyTargetService.updateCompanyTarget(id, updatedCompanyTarget);
		return (companyTarget != null) ? ResponseEntity.ok(companyTarget) : ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCompanyTarget(@PathVariable UUID id) {
		companyTargetService.deleteCompanyTarget(id);
		return ResponseEntity.noContent().build();
	}
}
