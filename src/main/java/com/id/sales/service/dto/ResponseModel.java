package com.id.sales.service.dto;

import java.util.Date;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ResponseModel {

	private String timestamp;
	private String success;
	private Integer statusCode;
	private String message;
	private Object data;

	public ResponseModel() {
		this.message = "Your request has been processed successfully";
		this.statusCode = HttpStatus.OK.value();
		this.success = "true";
		this.timestamp = String.valueOf(new Date().getTime());
	}
	
	public void conflict(String message) {
		this.message = message;
		this.statusCode = HttpStatus.CONFLICT.value();
		this.success = "false";
		this.timestamp = String.valueOf(new Date().getTime());
	}
	
	
	

}
