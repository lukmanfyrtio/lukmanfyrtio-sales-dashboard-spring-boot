package com.id.sales.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddRequestTarget {

	private String bup;
	private String tahun;
	private List<MonthClass> existing;
	private List<MonthClass> target;

}
