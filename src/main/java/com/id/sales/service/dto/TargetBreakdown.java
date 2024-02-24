package com.id.sales.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetBreakdown {

	private String salesFunnel;
	private String mtdTarget;
	private String existingCustomer;
	private String GAP;
	private String achievement;
	private String cashInTarget;
	private String mtdCashIn;
}
