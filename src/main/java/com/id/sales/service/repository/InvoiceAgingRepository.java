package com.id.sales.service.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import com.id.sales.service.model.InvoiceAging;

public interface InvoiceAgingRepository extends JpaRepository<InvoiceAging, Integer>,QuerydslPredicateExecutor<InvoiceAging>{

	
	@Query(value = "SELECT SUM(pokok_penerimaan) FROM invoice_aging "
	        + "WHERE MONTHNAME(STR_TO_DATE(tgl_masuk_rekening_pokok, '%d/%m/%Y')) = :bulan "
	        + "AND bup = :bup", nativeQuery = true)
	BigDecimal getPenerimaanPokokByBulan(@Param(value = "bulan") String bulan, @Param(value = "bup") String bup);

	@Query(value = "SELECT SUM(pokok_penerimaan) FROM invoice_aging "
	        + "WHERE MONTHNAME(STR_TO_DATE(tgl_masuk_rekening_pokok, '%d/%m/%Y')) = :bulan "
	        + "AND YEAR(STR_TO_DATE(tgl_masuk_rekening_pokok, '%d/%m/%Y')) = :tahun", nativeQuery = true)
	BigDecimal getPenerimaanPokokByBulanYear(@Param(value = "bulan") String bulan, @Param(value = "tahun") Integer tahun);

	@Query(value = "SELECT SUM(pokok_penerimaan) FROM invoice_aging "
	        + "WHERE MONTHNAME(STR_TO_DATE(tgl_masuk_rekening_pokok, '%d/%m/%Y')) = :bulan "
	        + "AND YEAR(STR_TO_DATE(tgl_masuk_rekening_pokok, '%d/%m/%Y')) = :tahun "
	        + "AND bup = :bup", nativeQuery = true)
	BigDecimal getPenerimaanPokokByBulanBUPYear(@Param(value = "bulan") String bulan,
	        @Param(value = "tahun") Integer tahun, @Param(value = "bup") String bup);

	
	@Query(value = "select DISTINCT YEAR(STR_TO_DATE(ia.tgl_masuk_rekening_pokok , '%d/%m/%Y')) as a from salesdb.invoice_aging ia WHERE ia.tgl_masuk_rekening_pokok !='' ", nativeQuery = true)
	List<Integer> getListYear();
}
