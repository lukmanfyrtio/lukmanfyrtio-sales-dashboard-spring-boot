package com.id.sales.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.id.sales.service.dto.AddRequestTarget;
import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.service.SalesInfoService;

@RestController
@RequestMapping("/sales-target")
@CrossOrigin
public class SalesInfoController {
	
	@Autowired
	private SalesInfoService salesInfoService;

	@GetMapping("/list")
	private ResponseModel getList(@RequestParam(required = false, name = "bulan") String bulan,
			@RequestParam(required = false, name = "tahun") String tahun,
			@RequestParam(required = false, name = "bup") String bup,
			@RequestParam(required = false, name = "search") String search,
			@RequestParam(required = true, name = "page") Integer page,
			@RequestParam(required = true, name = "size") Integer size) {
		
		ResponseModel response = new ResponseModel();
		response.setData(salesInfoService.listSalesInfo(bulan, tahun, bup, search, page, size));
		return response;
	}
	
	@PostMapping("/add")
	private ResponseModel addData(@RequestBody(required = true) AddRequestTarget body) {
		return salesInfoService.addSalesInfo(body);
		
	}
	
	@PostMapping("/edit")
	private ResponseModel editData(@RequestBody AddRequestTarget body) {
		return salesInfoService.editSalesInfo(body);
	}
	
	@DeleteMapping("/delete/{tahun}")
	private ResponseModel deleteData(@PathVariable("tahun") String tahun,@RequestParam(required = true, name = "bup") String bup) {
		return salesInfoService.deleteSalesInfo(Integer.valueOf(tahun),bup);
		
	}
	
	@DeleteMapping("/deleteall")
	private ResponseModel deleteDataall() {
		return salesInfoService.deleteSalesInfoAll();
		
	}
	
	
	@GetMapping("/detail/{tahun}")
	private ResponseModel detailData(@PathVariable("tahun") String tahun,@RequestParam(required = true, name = "bup") String bup) {
		return salesInfoService.detailSalesInfo(Integer.valueOf(tahun),bup);
		
	}
}
