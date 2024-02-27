package com.id.sales.service.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.id.sales.service.model.SalesRevenue;

public interface SalesRevenueRepository extends JpaRepository<SalesRevenue, UUID> {
	@Query(value = "SELECT SUM(principalReceipt) FROM SalesRevenue "
	        + "WHERE MONTHNAME(principalReceiptEntryDate) = :bulan "
	        + "AND YEAR(principalReceiptEntryDate) = :tahun "
	        + "AND department.id = :departmentId")
	BigDecimal getSumPrincipalReceiptByMonthDepartmentIdYear(@Param("bulan") String bulan,
	        @Param("tahun") Integer tahun, @Param("departmentId") UUID departmentId);

}