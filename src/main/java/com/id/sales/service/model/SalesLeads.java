package com.id.sales.service.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class SalesLeads extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank(message = "Month is required")
	private String month;

	@NotBlank(message = "Sales name is required")
	private String salesName;

	@NotBlank(message = "Potential customer is required")
	private String potentialCustomer;

	@NotBlank(message = "Address is required")
	private String address;

	@NotBlank(message = "Postal code is required")
	private String postalCode;

	@NotBlank(message = "Phone number is required")
	private String phoneNumber;

	@NotBlank(message = "Email is required")
	private String email;

	@NotNull(message = "Product is required")
	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;

	@NotBlank(message = "Projected value is required")
	private String projectedValue;

	private String leadsCategory;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date opportunitiesOpen;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date opportunitiesClose;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date proposalOpen;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date proposalClose;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date negotiationOpen;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date negotiationClose;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date dealsOpen;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date dealsClose;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date droppedOpen;
	@JsonFormat(pattern = "yyyy/MM/dd")
	private Date droppedClose;

	@NotBlank(message = "Current stage is required")
	@Column(name = "CURRENT_STAGE")
	private String currentStage;

	@NotBlank(message = "Leads status is required")
	@Column(name = "LEADS_STATUS")
	private String leadsStatus;
	private String notes;

	public long getCountDays() {
		SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");

		try {
			if (this.opportunitiesOpen != null) {
				if (leadsStatus.equalsIgnoreCase("Open")) {
					Date date1 = myFormat.parse(myFormat.format(this.opportunitiesOpen));
					Date date2 = myFormat.parse(myFormat.format(new Date()));
					long diff = date2.getTime() - date1.getTime();
					return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
				} else if (currentStage.equalsIgnoreCase("Deals")) {
					Date date1 = myFormat.parse(myFormat.format(this.opportunitiesOpen));
					Date date2 = myFormat.parse(myFormat.format(this.dealsOpen));
					long diff = date2.getTime() - date1.getTime();
					return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
				} else if (currentStage.equalsIgnoreCase("Dropped")) {
					Date date1 = myFormat.parse(myFormat.format(this.opportunitiesOpen));
					Date date2 = myFormat.parse(myFormat.format(this.droppedOpen));
					long diff = date2.getTime() - date1.getTime();
					return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
		return 0;
	}

}
