package com.id.sales.service.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddRequestTarget {

	private UUID departmentId;
	private String year;
	private List<MonthClass> existing;
	private List<MonthClass> target;

}
