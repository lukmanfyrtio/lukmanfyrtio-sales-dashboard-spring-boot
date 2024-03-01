package com.id.sales.service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.id.sales.service.model.Department;
import com.id.sales.service.model.QDepartment;
import com.id.sales.service.repository.DepartmentRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

	public List<Department> getAllDepartments() {
		return departmentRepository.findAllByOrderByNameDesc();
	}

	public Page<Department> filter(String search, Pageable pageable) {
		BooleanExpression predicate = QDepartment.department.id.isNotNull();
		if (search != null) {
			predicate = predicate.and(QDepartment.department.name.toLowerCase().like("%" + search.toLowerCase() + "%"))
					.or(QDepartment.department.company.name.like("%" + search.toLowerCase() + "%"));
		}
		return departmentRepository.findAll(predicate,pageable);
	}

	public Optional<Department> getDepartmentById(UUID departmentId) {
		return departmentRepository.findById(departmentId);
	}

	public Department createDepartment(Department department) {
		return departmentRepository.save(department);
	}

	public Department updateDepartment(UUID departmentId, Department updatedDepartment) {
		if (departmentRepository.existsById(departmentId)) {
			updatedDepartment.setId(departmentId);
			return departmentRepository.save(updatedDepartment);
		}
		return null; // Handle not found case
	}

	public void deleteDepartment(UUID departmentId) {
		departmentRepository.deleteById(departmentId);
	}

	public List<Department> getTop3DisplayableDepartments() {
		return departmentRepository.findTop3ByIsDisplayTrue();
	}
}