package com.id.sales.service.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.Department;
import com.id.sales.service.service.DepartmentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/departments")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

	@Autowired
	private DepartmentService departmentService;

	@GetMapping
	public List<Department> getAllDepartments() {
		return departmentService.getAllDepartments();
	}

	@GetMapping("/filter")
	public Page<Department> getAllDepartments(@RequestParam(required = false, name = "search") String search,
			@RequestParam(required = true, name = "page") Integer page,
			@RequestParam(required = true, name = "size") Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		return departmentService.filter(search, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Department> getDepartmentById(@PathVariable UUID id) {
		Optional<Department> department = departmentService.getDepartmentById(id);
		return department.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<?> createDepartment(@RequestBody Department department) {
		if (department.isDisplay() && departmentService.getTop3DisplayableDepartments().size() >= 3) {
		    ResponseModel model = new ResponseModel();
		    model.conflict("Display data is limited to a maximum of 3 departments.");
		    return ResponseEntity.status(HttpStatus.CONFLICT).body(model);
		}

		Department createdDepartment = departmentService.createDepartment(department);
		return ResponseEntity.ok(createdDepartment);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateDepartment(@PathVariable UUID id,
			@RequestBody Department updatedDepartment) {
		if (updatedDepartment.isDisplay() && departmentService.getTop3DisplayableDepartments().size() >= 3) {
		    ResponseModel model = new ResponseModel();
		    model.conflict("Display data is limited to a maximum of 3 departments.");
		    return ResponseEntity.status(HttpStatus.CONFLICT).body(model);
		}
		Department department = departmentService.updateDepartment(id, updatedDepartment);
		return (department != null) ? ResponseEntity.ok(department) : ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
		try {
			departmentService.deleteDepartment(id);
			return ResponseEntity.noContent().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
		}
	}

	@GetMapping("/display/top3")
	public ResponseEntity<List<Department>> getTop3DisplayableDepartments() {
		List<Department> departments = departmentService.getTop3DisplayableDepartments();
		return ResponseEntity.ok(departments);
	}
}