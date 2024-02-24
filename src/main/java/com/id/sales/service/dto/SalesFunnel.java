package com.id.sales.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesFunnel {

	private String dropped;
	private String deals;
	private String negotiation;
	private String proposal;
	private String opportunities;
	private String leadsConvertionRate;
	private String avgSalesCycle;
	private String lastUpdated;
	
	
}
