package com.id.sales.service.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.id.sales.service.model.CustomerInfo;

@Repository
public interface CustomerInfoRepository
		extends JpaRepository<CustomerInfo, Long>, QuerydslPredicateExecutor<CustomerInfo> {

	@Query(value = "select a from CustomerInfo a where a.bup=:bup")
	List<CustomerInfo> getList(@Param("bup") String bup, Pageable pageable);

	@Query(value = "select a from CustomerInfo a where calonPelanggan=:calonPelanggan and project=:project and bup=:bup and produk=:produk")
	CustomerInfo getByUId(@Param("calonPelanggan") String calonPelanggan, @Param("project") String project,
			@Param("bup") String bup, @Param("produk") String produk);

	@Query(value = "SELECT SUM(proyeksi_nilai) FROM customer_info "
			+ "WHERE MONTH(STR_TO_DATE(opportunities_open, '%d/%m/%Y')) = :bulan "
			+ "AND YEAR(STR_TO_DATE(opportunities_open, '%d/%m/%Y')) = :tahun", nativeQuery = true)
	BigDecimal getProspekNilaiByBulanYear(@Param(value = "bulan") String bulan, @Param(value = "tahun") Integer tahun);

	@Query(value = "SELECT * FROM customer_info a " + "WHERE a.bup = :bup "
			+ "AND YEAR(STR_TO_DATE(a.opportunities_open, '%d/%m/%Y')) = :tahun", nativeQuery = true)
	List<CustomerInfo> getListByBUPAndYear(@Param("bup") String bup, @Param(value = "tahun") Integer tahun);

	@Query(value = "SELECT * FROM customer_info a " + "WHERE a.bup = :bup "
			+ "AND YEAR(STR_TO_DATE(a.opportunities_open, '%d/%m/%Y')) = :tahun "
			+ "AND a.proyeksi_nilai IS NOT NULL", nativeQuery = true)
	List<CustomerInfo> getListByBUPAndYearIsNotNull(@Param("bup") String bup, @Param(value = "tahun") Integer tahun);

	@Query(value = "SELECT * FROM customer_info a " + "WHERE a.bup = :bup "
			+ "AND YEAR(STR_TO_DATE(a.opportunities_open, '%d/%m/%Y')) = :tahun "
			+ "AND a.current_stage = :currentStage " + "AND a.leads_status = :leadsStatus", nativeQuery = true)
	List<CustomerInfo> getListByStageBUPAndYear(@Param("bup") String bup, @Param(value = "tahun") Integer tahun,
			@Param("currentStage") String currentStage, @Param("leadsStatus") String leadsStatus);

	@Query(value = "SELECT * FROM customer_info a " + "WHERE a.bup = :bup "
			+ "AND YEAR(STR_TO_DATE(a.opportunities_open, '%d/%m/%Y')) = :tahun "
			+ "AND a.current_stage = :currentStage " + "AND a.leads_status = :leadsStatus "
			+ "AND a.sales_name = :salesName", nativeQuery = true)
	List<CustomerInfo> getListByStageBUPAndYearSalesName(@Param("bup") String bup,
			@Param(value = "tahun") Integer tahun, @Param("currentStage") String currentStage,
			@Param("leadsStatus") String leadsStatus, @Param("salesName") String salesName);

	@Query(value = "SELECT a.updated_time FROM customer_info a where a.bup=:bup and CAST(YEAR(STR_TO_DATE(a.opportunities_open , '%d/%m/%Y'))AS SIGNED) = :tahun ORDER BY a.updated_time DESC LIMIT 1", nativeQuery = true)
	Date getLastUpdate(@Param("bup") String bup, @Param(value = "tahun") Integer tahun);

	@Query(value = "SELECT a.created_time FROM customer_info a where a.bup=:bup and CAST(YEAR(STR_TO_DATE(a.opportunities_open , '%d/%m/%Y'))AS SIGNED) = :tahun ORDER BY a.created_time DESC LIMIT 1", nativeQuery = true)
	Date getLastCreated(@Param("bup") String bup, @Param(value = "tahun") Integer tahun);

	@Query(value = "select distinct calon_pelanggan from customer_info", nativeQuery = true)
	List<String> getCustomerInfo();
}
