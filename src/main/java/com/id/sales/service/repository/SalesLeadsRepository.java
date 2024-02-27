package com.id.sales.service.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.id.sales.service.model.SalesLeads;

public interface SalesLeadsRepository extends JpaRepository<SalesLeads, UUID> {
    @Query("SELECT sl FROM SalesLeads sl WHERE " +
            "(:currentStage IS NULL OR sl.currentStage = :currentStage) AND " +
            "(:leadStatus IS NULL OR sl.leadsStatus = :leadStatus) AND " +
            "(:departmentId IS NULL OR sl.product.department.id = :departmentId) AND " +
            "(:search IS NULL OR LOWER(sl.salesName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(sl.potentialCustomer) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SalesLeads> filterSalesLeads(
            @Param("currentStage") String currentStage,
            @Param("leadStatus") String leadStatus,
            @Param("departmentId") UUID departmentId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(value = "SELECT a FROM SalesLeads a " +
            "WHERE a.product.department.id = :departmentId " +
            "AND YEAR(a.opportunitiesOpen) = :year")
    List<SalesLeads> getListByDepartmentIdAndYear(@Param("departmentId") UUID departmentId, @Param("year") Integer year);
    
	@Query(value="SELECT a FROM SalesLeads a "
	        + "WHERE a.product.department.id = :departmentId "
	        + "AND YEAR(a.opportunitiesOpen) = :year "
	        + "AND a.currentStage = :currentStage "
	        + "AND a.leadsStatus = :leadsStatus")
	List<SalesLeads> getListByStageBUPAndYear(@Param("departmentId") UUID departmentId,
	        @Param(value = "year") Integer year,
	        @Param("currentStage") String currentStage,
	        @Param("leadsStatus") String leadsStatus);
	
	@Query(value="SELECT a FROM SalesLeads a "
	        + "WHERE a.product.department.id = :departmentId "
	        + "AND YEAR(a.opportunitiesOpen) = :year "
	        + "AND a.currentStage = :currentStage "
	        + "AND a.leadsStatus = :leadsStatus "
	        + "AND a.salesName = :salesName")
	List<SalesLeads> getListByStageBUPAndYearSalesName(@Param("departmentId") UUID departmentId,
	        @Param(value = "year") Integer year,
	        @Param("currentStage") String currentStage,
	        @Param("leadsStatus") String leadsStatus, 
	        @Param("salesName") String salesName);
	
	
	@Query(value="SELECT a.updatedAt FROM SalesLeads a "
			+ "WHERE a.product.department.id = :departmentId "
			+ "AND YEAR(a.opportunitiesOpen) = :year "
			+ "ORDER BY a.updatedAt DESC LIMIT 1")
	Date getLastUpdate(@Param("departmentId") UUID departmentId
			,@Param(value = "year") Integer year);
	
	@Query(value="SELECT a.createdAt FROM SalesLeads a "
			+ "WHERE a.product.department.id = :departmentId "
			+ "AND YEAR(a.opportunitiesOpen) = :year "
			+ "ORDER BY a.createdAt DESC LIMIT 1")
	Date getLastCreated(@Param("departmentId") UUID departmentId
			,@Param(value = "year") Integer year);
}

