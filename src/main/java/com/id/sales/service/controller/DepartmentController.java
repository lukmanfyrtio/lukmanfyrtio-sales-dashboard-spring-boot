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

	@GetMapping("/{id}")
	public ResponseEntity<Department> getDepartmentById(@PathVariable UUID id) {
		Optional<Department> department = departmentService.getDepartmentById(id);
		return department.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
		Department createdDepartment = departmentService.createDepartment(department);
		return ResponseEntity.ok(createdDepartment);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Department> updateDepartment(@PathVariable UUID id,
			@RequestBody Department updatedDepartment) {
		Department department = departmentService.updateDepartment(id, updatedDepartment);
		return (department != null) ? ResponseEntity.ok(department) : ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
		departmentService.deleteDepartment(id);
		return ResponseEntity.noContent().build();
	}
	
    @GetMapping("/display/top3")
    public ResponseEntity<List<Department>> getTop3DisplayableDepartments() {
        List<Department> departments = departmentService.getTop3DisplayableDepartments();
        return ResponseEntity.ok(departments);
    }
}