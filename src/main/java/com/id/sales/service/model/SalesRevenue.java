package com.id.sales.service.model;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SalesRevenue extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	private String invoiceNumber;
	private String invoiceDate;

	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date dueDate;
	private String principalReceipt;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date principalReceiptEntryDate;
	@Column(length = 1000, name = "notes")
	private String notes;

	@ManyToOne
	@JoinColumn(name = "department_id")
	private Department department;

	@ManyToOne
	@JoinColumn(name = "sales_leads_id")
	private SalesLeads salesLeads;

}
