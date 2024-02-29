package com.id.sales.service.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailResponse {
	
	private String year;
	private UUID departmentId;
	private MonthName existing;
	private MonthName target;

}
