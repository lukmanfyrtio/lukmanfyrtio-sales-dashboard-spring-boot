package com.id.sales.service.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.id.sales.service.model.SalesRevenue;
import com.id.sales.service.service.SalesRevenueService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/sales-revenue")
@SecurityRequirement(name = "bearerAuth")
public class SalesRevenueController {

	@Autowired
	private SalesRevenueService salesRevenueService;

	@GetMapping("/{id}")
	public ResponseEntity<SalesRevenue> getSalesRevenueById(@PathVariable UUID id) {
		Optional<SalesRevenue> salesRevenue = salesRevenueService.getSalesRevenueById(id);
		return salesRevenue.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping
	public ResponseEntity<List<SalesRevenue>> getAllSalesRevenue() {
		List<SalesRevenue> salesRevenues = salesRevenueService.getAllSalesRevenues();
		return ResponseEntity.ok(salesRevenues);
	}

	@PostMapping
	public ResponseEntity<SalesRevenue> createSalesRevenue(@RequestBody SalesRevenue salesRevenue) {
		SalesRevenue createdSalesRevenue = salesRevenueService.createSalesRevenue(salesRevenue);
		return new ResponseEntity<>(createdSalesRevenue, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SalesRevenue> updateSalesRevenue(@PathVariable UUID id,
			@RequestBody SalesRevenue salesRevenue) {
		SalesRevenue updatedSalesRevenue = salesRevenueService.updateSalesRevenue(id, salesRevenue);
		return ResponseEntity.ok(updatedSalesRevenue);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSalesRevenue(@PathVariable UUID id) {
		salesRevenueService.deleteSalesRevenue(id);
		return ResponseEntity.noContent().build();
	}

}
