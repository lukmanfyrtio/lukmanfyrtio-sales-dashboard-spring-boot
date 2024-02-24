package com.id.sales.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailResponse {
	
	private String tahun;
	private String bup;
	private MonthName existing;
	private MonthName target;

}
