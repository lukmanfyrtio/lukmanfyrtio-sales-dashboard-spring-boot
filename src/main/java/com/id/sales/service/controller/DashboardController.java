package com.id.sales.service.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.CustomerInfo;
import com.id.sales.service.service.DashboardService;
import com.id.sales.service.service.InvoiceAgingService;

@RestController
@CrossOrigin
@RequestMapping("/dashboard")
public class DashboardController {

	@Autowired
	private DashboardService dashService;

	@Autowired
	private InvoiceAgingService invoiceAgingService;

	@PostMapping("/upload/excel2")
	private ResponseModel uploadExcel2(@RequestParam(required = true, name = "file") MultipartFile file) {
		return dashService.excelReadTable(file);
	}

	@GetMapping("/list")
	private ResponseModel getList(@RequestParam(required = false, name = "stage") String stage,
			@RequestParam(required = false, name = "status") String status,
			@RequestParam(required = false, name = "bup") String bup,
			@RequestParam(required = false, name = "search") String search,
			@RequestParam(required = true, name = "page") Integer page,
			@RequestParam(required = true, name = "size") Integer size) {

		ResponseModel response = new ResponseModel();
		response.setData(dashService.listCustomerInfo(stage, status, bup, search, page, size));
		return response;
	}

	@PostMapping("/add")
	private ResponseModel addData(@RequestBody(required = true) CustomerInfo body, Authentication auth) {
		Jwt user = (Jwt) auth.getPrincipal();
		return dashService.addCustomer(body, user.getClaimAsString("username"));

	}

	@DeleteMapping("/deleteall")
	private ResponseModel deleteDataall() {
		return dashService.deleteCustomer();

	}

	@PostMapping("/edit/{id}")
	private ResponseModel editData(@PathVariable("id") String id, @RequestBody CustomerInfo body, Authentication auth) {
		Jwt user = (Jwt) auth.getPrincipal();
		return dashService.editCustomer(id, body, user.getClaimAsString("username"));

	}

	@DeleteMapping("/delete/{id}")
	private ResponseModel deleteData(@PathVariable("id") String id) {
		return dashService.deleteCustomer(Long.valueOf(id));

	}

	@DeleteMapping("/detail/{id}")
	private ResponseModel detailData(@PathVariable("id") String id) {
		return dashService.detailCustomer(Long.valueOf(id));

	}

	@GetMapping("/target-breakdown")
	private ResponseModel getTargetAndBreakDown(@RequestParam("tahun") String tahun,
			@RequestParam(value = "bup", required = false) String bup) {
		return dashService.getTargetBreakdown(tahun, bup);

	}

	@GetMapping("/target-breakdown-all")
	private ResponseModel getTargetAndBreakDownAll(@RequestParam("tahun") String tahun) {
		return dashService.getTargetBreakdownALL(tahun);

	}

	@GetMapping("/target-actual")
	private ResponseModel getTargetActual(@RequestParam("tahun") String tahun,
			@RequestParam(value = "bup", required = false) String bup) {
		return dashService.getTargetVsActual(tahun, bup);

	}

	@GetMapping("/existing-gap")
	private ResponseModel getExistingGAP(@RequestParam("tahun") String tahun,
			@RequestParam(value = "bup", required = false) String bup) {
		return dashService.getExistGAP(tahun, bup);

	}

	@GetMapping("/detail")
	private ResponseModel funnelData(@RequestParam(required = true, name = "bup") String bup,
			@RequestParam(required = true, name = "tahun") String tahun) {
		return dashService.getDetailDashboard(bup, tahun);
	}

	@GetMapping("/list-sales")
	private ResponseModel listSales(@RequestParam(required = true, name = "bup") String bup,
			@RequestParam(required = true, name = "tahun") String tahun,
			@RequestParam(required = true, name = "page") Integer page,
			@RequestParam(required = true, name = "size") Integer size) {
		return dashService.getDataSales(bup, tahun, page, size);
	}

	@GetMapping("/avg-sales")
	private ResponseModel avgSalesCycle(@RequestParam(required = true, name = "bup") String bup,
			@RequestParam(required = true, name = "tahun") String tahun) {
		return dashService.getAvgSales(bup, tahun);
	}

	@GetMapping("/download")
	public ResponseEntity<Resource> getFile() {
		String filename = "Customer_"+new SimpleDateFormat("YYYYmmddhhmmss").format(new Date())+".xlsx";
		InputStreamResource file = new InputStreamResource(invoiceAgingService.loadCustomer());
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
	}
	
	@GetMapping("/customers")
	public ResponseModel getCustomers() {
		ResponseModel model = new ResponseModel();
		model.setData(dashService.getCustomer());
		return model;
	}

}
