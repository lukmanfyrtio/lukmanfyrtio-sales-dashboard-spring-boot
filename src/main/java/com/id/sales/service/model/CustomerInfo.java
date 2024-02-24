package com.id.sales.service.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CUSTOMER_INFO")
public class CustomerInfo extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idPelanggan;

	private String bulan;
	@Column(name = "BUP")
	private String bup;
	private String salesName;
	private String calonPelanggan;
	private String alamat;
	private String kelurahan;
	private String kecamatan;
	private String kabupaten;
	private String provinsi;
	private String kodePos;
	private String nomerTelepon;
	private String email;
	private String project;
	private String produk;
	private String proyeksiNilai;
	private String leadsCategory;
	private String opportunitiesOpen;
	private String opportunitiesClose;
	private String proposalOpen;
	private String proposalClose;
	private String negotiationOpen;
	private String negotiationClose;
	private String dealsOpen;
	private String dealsClose;
	private String droppedOpen;
	private String droppedClose;
	@Column(name = "CURRENT_STAGE")
	private String currentStage;
	@Column(name = "LEADS_STATUS")
	private String leadsStatus;
	private String keterangan;

	private Date createdTime;

	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	@Column(name = "created_time", nullable = true, insertable = true, updatable = false)
	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	private String createdBy;

	@Column(name = "created_by", nullable = true, insertable = true, updatable = false, length = 25, precision = 0)
	@CreatedBy
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	private Date updatedTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time", nullable = true, insertable = true, updatable = true)
	@LastModifiedDate
	@CreatedDate
	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	private String updatedBy;

	@Column(name = "updated_by", nullable = true, insertable = true, updatable = true, length = 25, precision = 0)
	@LastModifiedBy
	@CreatedBy
	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public long getCountDays() {
		SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");

		try {
			if (this.opportunitiesOpen != null) {
				if (leadsStatus.equalsIgnoreCase("Open")) {
					Date date1 = myFormat.parse(opportunitiesOpen);
					Date date2 = myFormat.parse(myFormat.format(new Date()));
					long diff = date2.getTime() - date1.getTime();
					return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
				} else if (currentStage.equalsIgnoreCase("Deals")) {
					Date date1 = myFormat.parse(opportunitiesOpen);
					Date date2 = myFormat.parse(dealsOpen);
					long diff = date2.getTime() - date1.getTime();
					return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
				} else if (currentStage.equalsIgnoreCase("Dropped")) {
					Date date1 = myFormat.parse(opportunitiesOpen);
					Date date2 = myFormat.parse(droppedOpen);
					long diff = date2.getTime() - date1.getTime();
					return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
