package com.id.sales.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "INVOICE_AGING")
public class InvoiceAging {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String bup;
	private String tenant;
	private String nomerInvoice;
	private String tglInvoice;
	private String tglInvoiceDiterimaTenant;
	private String tglJatuhTempo;
	private String pokokPenerimaan;
	private String tglMasukRekeningPokok;
	@Column(length = 1000, name = "keterangan")
	private String keterangan;

	private String jatuhTempo;
	private String agingInvoiceSejakDiterima;
	private String denda;
	private String keteranganAgingInvoice;
	private String agingPembayaran;

}
