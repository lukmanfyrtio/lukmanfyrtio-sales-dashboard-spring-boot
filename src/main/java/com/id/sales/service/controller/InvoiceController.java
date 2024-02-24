package com.id.sales.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.InvoiceAging;
import com.id.sales.service.service.InvoiceAgingService;

@RestController
@CrossOrigin
@RequestMapping("/invoice")
public class InvoiceController {

	@Autowired
	private InvoiceAgingService invoiceAgingService;

	@GetMapping("/list")
	private ResponseModel getList(@RequestParam(required = false, name = "tenant") String tenant,
			@RequestParam(required = false, name = "status") String status,
			@RequestParam(required = false, name = "denda") String denda,
			@RequestParam(required = false, name = "bup") String bup,
			@RequestParam(required = false, name = "search") String search,
			@RequestParam(required = true, name = "page") Integer page,
			@RequestParam(required = true, name = "size") Integer size) {

		ResponseModel response = new ResponseModel();
		response.setData(invoiceAgingService.listSalesInfo(tenant, denda, status, bup, search, page, size));
		return response;
	}

	@PostMapping("/add")
	private ResponseModel addData(@RequestBody(required = true) InvoiceAging body) throws Exception {
		return invoiceAgingService.addInvoice(body);

	}

	@PostMapping("/edit/{id}")
	private ResponseModel editData(@PathVariable("id") String id, @RequestBody InvoiceAging body) {
		return invoiceAgingService.editInvoice(id, body);

	}

	@DeleteMapping("/delete/{id}")
	private ResponseModel deleteData(@PathVariable("id") String id) {
		return invoiceAgingService.deleteInvoice(Integer.valueOf(id));

	}

	@DeleteMapping("/detail/{id}")
	private ResponseModel detailData(@PathVariable("id") String id) {
		return invoiceAgingService.detailInvoice(Integer.valueOf(id));

	}

	@DeleteMapping("/deleteall")
	private ResponseModel deleteDataall() {
		return invoiceAgingService.deleteINV();

	}

	@GetMapping("/download")
	public ResponseEntity<Resource> getFile() {
		String filename = "InvoiceAging.xlsx";
		InputStreamResource file = new InputStreamResource(invoiceAgingService.load());
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
	}
}
