package com.id.sales.service.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.SalesLeads;
import com.id.sales.service.service.SalesLeadsService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/salesleads")
@SecurityRequirement(name = "bearerAuth")
public class SalesLeadsController {

	@Autowired
	private SalesLeadsService salesLeadsService;

	@GetMapping
	public List<SalesLeads> getAllSalesLeads() {
		try {
			return salesLeadsService.getAllSalesLeads();
		} catch (Exception e) {
			throw e;
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<SalesLeads> getSalesLeadsById(@PathVariable UUID id) {
		Optional<SalesLeads> salesLeads = salesLeadsService.getSalesLeadsById(id);
		return salesLeads.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<?> createSalesLeads(@Valid @RequestBody SalesLeads salesLeads, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return ResponseEntity.badRequest()
					.body("Validation error: " + bindingResult.getFieldError().getDefaultMessage());
		}

		SalesLeads createdSalesLeads = salesLeadsService.createSalesLeads(salesLeads);
		return ResponseEntity.ok(createdSalesLeads);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SalesLeads> updateSalesLeads(@PathVariable UUID id,
			@RequestBody SalesLeads updatedSalesLeads) {
		SalesLeads salesLeads = salesLeadsService.updateSalesLeads(id, updatedSalesLeads);
		return (salesLeads != null) ? ResponseEntity.ok(salesLeads) : ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSalesLeads(@PathVariable UUID id) {
		salesLeadsService.deleteSalesLeads(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/filter")
	public ResponseEntity<Page<SalesLeads>> filterSalesLeads(
			@RequestParam(required = false, name = "stage") String currentStage,
			@RequestParam(required = false, name = "status") String leadStatus,
			@RequestParam(required = false, name = "departmentId") UUID departmentId,
			@RequestParam(required = false, name = "search") String search,
			@RequestParam(required = true, name = "page") Integer page,
			@RequestParam(required = true, name = "size") Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<SalesLeads> filteredSalesLeads = salesLeadsService.filterSalesLeads(currentStage, leadStatus, departmentId,
				search, pageable);
		return ResponseEntity.ok(filteredSalesLeads);
	}
	
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportSalesLeadsToExcel() throws IOException {
        List<SalesLeads> salesLeadsList = salesLeadsService.getAllSalesLeads();
        ByteArrayInputStream in = salesLeadsService.exportSalesLeadsToExcel(salesLeadsList);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=sales_leads.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }
    
    @PostMapping("/import")
	private ResponseModel uploadExcel2(@RequestParam(required = true, name = "file") MultipartFile file) {
		try {
			return salesLeadsService.importSalesLeads(file);
		} catch (IOException e) {
			ResponseModel model=new ResponseModel();
			model.setData(null);
			model.setMessage(e.getMessage());
			model.setSuccess("false");
			model.setStatusCode(400);
			return model;
		}
	}
}
