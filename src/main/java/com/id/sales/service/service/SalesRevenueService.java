package com.id.sales.service.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.id.sales.service.model.QSalesRevenue;
import com.id.sales.service.model.SalesRevenue;
import com.id.sales.service.repository.SalesRevenueRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class SalesRevenueService {

	@Autowired
    private SalesRevenueRepository salesRevenueRepository;

    public List<SalesRevenue> getAllSalesRevenues() {
        return salesRevenueRepository.findAll();
    }

    public Optional<SalesRevenue> getSalesRevenueById(UUID salesRevenueId) {
        return salesRevenueRepository.findById(salesRevenueId);
    }

    public SalesRevenue createSalesRevenue(SalesRevenue salesRevenue) {
        return salesRevenueRepository.save(salesRevenue);
    }

    public SalesRevenue updateSalesRevenue(UUID salesRevenueId, SalesRevenue updatedSalesRevenue) {
        if (salesRevenueRepository.existsById(salesRevenueId)) {
            updatedSalesRevenue.setId(salesRevenueId);
            return salesRevenueRepository.save(updatedSalesRevenue);
        }
        return null; // Handle not found case
    }

    public void deleteSalesRevenue(UUID salesRevenueId) {
        salesRevenueRepository.deleteById(salesRevenueId);
    }
    
	public Page<SalesRevenue> filterSalesRevenues(UUID departmentUUID, String search, Pageable pageable) {
		BooleanExpression predicate = QSalesRevenue.salesRevenue.id.isNotNull();
		if (departmentUUID != null) {
			predicate = predicate.and(QSalesRevenue.salesRevenue.department.id.eq(departmentUUID));
		}

		if (search != null) {
			predicate = predicate.and(QSalesRevenue.salesRevenue.invoiceNumber.toUpperCase()
					.like("%" + search.toUpperCase() + "%")
					.or(QSalesRevenue.salesRevenue.department.name.toUpperCase().like("%" + search.toUpperCase() + "%"))
					.or(QSalesRevenue.salesRevenue.invoiceNumber.toUpperCase().like("%" + search.toUpperCase() + "%"))
					.or(QSalesRevenue.salesRevenue.salesLeads.potentialCustomer.toUpperCase()
							.like("%" + search.toUpperCase() + "%"))
					.or(QSalesRevenue.salesRevenue.salesLeads.product.name.toUpperCase()
							.like("%" + search.toUpperCase() + "%")));
		}
		Page<SalesRevenue> list = salesRevenueRepository.findAll(predicate, pageable);
		return list;
	}
	
	
    public ByteArrayInputStream exportSalesRevenueToExcel(List<SalesRevenue> salesRevenueList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("SalesRevenue");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Invoice Number", "Invoice Date", "Due Date", "Principal Receipt(Rp.)",
                    "Principal Receipt Entry Date", "Notes", "Department", "Sales Leads"};

            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/MM/dd"));

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (SalesRevenue salesRevenue : salesRevenueList) {
                Row row = sheet.createRow(rowNum++);
                // Populate cells based on your SalesRevenue object structure
                // For example:
                row.createCell(0).setCellValue(salesRevenue.getInvoiceNumber());
                row.createCell(1).setCellValue(salesRevenue.getInvoiceDate());
                formatCellWithDateStyle(row.createCell(2), salesRevenue.getDueDate(), dateCellStyle);
                row.createCell(3).setCellValue(salesRevenue.getPrincipalReceipt());
                formatCellWithDateStyle(row.createCell(4), salesRevenue.getPrincipalReceiptEntryDate(), dateCellStyle);
                row.createCell(5).setCellValue(salesRevenue.getNotes());
                row.createCell(6).setCellValue(salesRevenue.getDepartment().getName()); // Adjust as per your Department class
                row.createCell(7).setCellValue(salesRevenue.getSalesLeads().getSalesName()); // Adjust as per your SalesLeads class
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void formatCellWithDateStyle(Cell cell, Date date, CellStyle dateCellStyle) {
        if (date != null) {
            cell.setCellValue(date);
            cell.setCellStyle(dateCellStyle);
        }
    }
}