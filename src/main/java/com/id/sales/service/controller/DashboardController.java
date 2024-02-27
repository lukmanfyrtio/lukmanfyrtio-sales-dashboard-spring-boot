package com.id.sales.service.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.service.DashboardService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

	@Autowired
	private DashboardService dashboardService;

	@GetMapping("/target-breakdown")
	private ResponseModel getTargetAndBreakDown(@RequestParam("year") String year,
			@RequestParam(value = "departementUUID", required = false) UUID departementUUID) {
		return dashboardService.getTargetBreakdown(year, departementUUID);

	}

	@GetMapping("/target-breakdown-all")
	private ResponseModel getTargetAndBreakDownAll(@RequestParam("tahun") String tahun) {
		return dashboardService.getTargetBreakdownALL(tahun);

	}

	@GetMapping("/target-actual")
	private ResponseModel getTargetActual(@RequestParam("tahun") String tahun,
			@RequestParam(value = "departementUUID", required = false) UUID departementUUID) {
		return dashboardService.getTargetVsActual(tahun, departementUUID);

	}

	@GetMapping("/existing-gap")
	private ResponseModel getExistingGAP(@RequestParam("tahun") String tahun,
			@RequestParam(value = "departementUUID", required = false) UUID departementUUID) {
		return dashboardService.getExistGAP(tahun, departementUUID);

	}

	@GetMapping("/detail")
	private ResponseModel funnelData(@RequestParam(value = "departementUUID", required = false) UUID departementUUID,
			@RequestParam(required = true, name = "tahun") String tahun) {
		return dashboardService.getDetailDashboard(departementUUID, tahun);
	}

	@GetMapping("/list-sales")
	private ResponseModel listSales(@RequestParam(value = "departementUUID", required = false) UUID departementUUID,
			@RequestParam(required = true, name = "tahun") String tahun,
			@RequestParam(required = true, name = "page") Integer page,
			@RequestParam(required = true, name = "size") Integer size) {
		return dashboardService.getDataSales(departementUUID, tahun, page, size);
	}

	@GetMapping("/avg-sales")
	private ResponseModel avgSalesCycle(@RequestParam(value = "departementUUID", required = false) UUID departementUUID,
			@RequestParam(required = true, name = "tahun") String tahun) {
		return dashboardService.getAvgSales(departementUUID, tahun);
	}
}
