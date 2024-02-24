package com.id.sales.service.dto;

import com.id.sales.service.model.CustomerInfo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExcelResponse {

	public ExcelResponse() {
		// TODO Auto-generated constructor stub
	}
	private boolean isValid;
	private String validationMessage;
	private CustomerInfo customerInfo;
}
