package com.id.sales.service.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CompanyTarget extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String month;
	private String year;
	private BigDecimal existing;
	private BigDecimal target;

	@ManyToOne
	@JoinColumn(name = "department_id")
	@NotNull(message = "Department ID is required")
	private Department department;

}
