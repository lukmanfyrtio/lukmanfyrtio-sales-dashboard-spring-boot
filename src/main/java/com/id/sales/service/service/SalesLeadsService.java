package com.id.sales.service.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.id.sales.service.dto.ExcelResponse;
import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.Product;
import com.id.sales.service.model.SalesLeads;
import com.id.sales.service.repository.ProductRepository;
import com.id.sales.service.repository.SalesLeadsRepository;

@Service
public class SalesLeadsService {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,6}$");
	private static final List<String> VALID_LEADS_STATUS = List.of("Open", "Close");
	private static final List<String> VALID_CURRENT_STAGES = List.of("Opportunities", "Proposal", "Negotiation",
			"Deals");

	@Autowired
	private SalesLeadsRepository salesLeadsRepository;

	@Autowired
	private ProductRepository productRepository;

	public List<SalesLeads> getAllSalesLeads() {
		try {
			return salesLeadsRepository.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	public Optional<SalesLeads> getSalesLeadsById(UUID salesLeadsId) {
		return salesLeadsRepository.findById(salesLeadsId);
	}

	public SalesLeads createSalesLeads(SalesLeads salesLeads) {
		updateDatesOnAdd(salesLeads);
		return salesLeadsRepository.save(salesLeads);
	}

	private void updateDatesOnAdd(SalesLeads data) {
		Date currentDate = new Date();

		if (data.getCurrentStage() != null) {
			switch (data.getCurrentStage()) {
			case "Opportunities":
				if (data.getOpportunitiesOpen() == null) {
					data.setOpportunitiesOpen(currentDate);
				}
				break;
			case "Proposal":
				if (data.getOpportunitiesClose() == null) {
					data.setOpportunitiesClose(currentDate);
				}
				data.setProposalOpen(currentDate);
				break;
			case "Negotiation":
				if (data.getProposalClose() == null) {
					data.setProposalClose(currentDate);
				}
				data.setNegotiationOpen(currentDate);
				break;
			case "Deals":
				if (data.getNegotiationClose() == null) {
					data.setNegotiationClose(currentDate);
				}
				data.setDealsOpen(currentDate);
				data.setDealsClose(currentDate);
				break;
			case "Dropped":
				if (data.getDroppedOpen() == null) {
					data.setDroppedOpen(currentDate);
				}
				if (data.getDroppedClose() == null) {
					data.setDroppedClose(currentDate);
				}
				break;
			default:
				break;
			}
		}
	}

	public SalesLeads updateSalesLeads(UUID salesLeadsId, SalesLeads updatedSalesLeads) {
		if (salesLeadsRepository.existsById(salesLeadsId)) {
			updatedSalesLeads.setId(salesLeadsId);
			updateDatesOnAdd(updatedSalesLeads);
			return salesLeadsRepository.save(updatedSalesLeads);
		}
		return null; // Handle not found case
	}

	public void deleteSalesLeads(UUID salesLeadsId) {
		salesLeadsRepository.deleteById(salesLeadsId);
	}

	public Page<SalesLeads> filterSalesLeads(String currentStage, String leadStatus, UUID departmentId, String search,
			Pageable pageable) {
		return salesLeadsRepository.filterSalesLeads(currentStage, leadStatus, departmentId, search, pageable);
	}

	public ByteArrayInputStream exportSalesLeadsToExcel(List<SalesLeads> salesLeadsList) throws IOException {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("SalesLeads");

			// Create header row
			Row headerRow = sheet.createRow(0);
			String[] columns = { "Month", "Sales Name", "Potential Customer", "Address", "Postal Code", "Phone Number",
					"Email", "Product", "Projected Value(Rp.)", "Leads Category", "Opportunities Open",
					"Opportunities Close", "Proposal Open", "Proposal Close", "Negotiation Open", "Negotiation Close",
					"Deals Open", "Deals Close", "Dropped Open", "Dropped Close", "Current Stage", "Leads Status",
					"Notes" };

			CellStyle dateCellStyle = workbook.createCellStyle();
			CreationHelper createHelper = workbook.getCreationHelper();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/MM/dd"));

			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
			}

			// Populate data rows
			int rowNum = 1;
			for (SalesLeads salesLeads : salesLeadsList) {
				Row row = sheet.createRow(rowNum++);
				// Populate cells based on your SalesLeads object structure
				// For example:
				row.createCell(0).setCellValue(salesLeads.getMonth());
				row.createCell(1).setCellValue(salesLeads.getSalesName());
				row.createCell(2).setCellValue(salesLeads.getPotentialCustomer());
				row.createCell(3).setCellValue(salesLeads.getAddress());
				row.createCell(4).setCellValue(salesLeads.getPostalCode());
				row.createCell(5).setCellValue(salesLeads.getPhoneNumber());
				row.createCell(6).setCellValue(salesLeads.getEmail());
				row.createCell(7).setCellValue(salesLeads.getProduct().getName()); // Adjust as per your Product class
				row.createCell(8).setCellValue(salesLeads.getProjectedValue());
				row.createCell(9).setCellValue(salesLeads.getLeadsCategory());

				formatCellWithDateStyle(row.createCell(10), salesLeads.getOpportunitiesOpen(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(11), salesLeads.getOpportunitiesClose(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(12), salesLeads.getProposalOpen(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(13), salesLeads.getProposalClose(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(14), salesLeads.getNegotiationOpen(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(15), salesLeads.getNegotiationClose(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(16), salesLeads.getDealsOpen(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(17), salesLeads.getDealsClose(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(18), salesLeads.getDroppedOpen(), dateCellStyle);
				formatCellWithDateStyle(row.createCell(19), salesLeads.getDroppedClose(), dateCellStyle);

				row.createCell(20).setCellValue(salesLeads.getCurrentStage());
				row.createCell(21).setCellValue(salesLeads.getLeadsStatus());
				row.createCell(22).setCellValue(salesLeads.getNotes());
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

	@Transactional
	public ResponseModel importSalesLeads(MultipartFile file) throws IOException {
		ResponseModel response = new ResponseModel();
		List<ExcelResponse> customerInfoList = new ArrayList<ExcelResponse>();

		try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {

			Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				StringBuilder validationMessage = new StringBuilder("");
				ExcelResponse excelResponse = new ExcelResponse();
				Row row = sheet.getRow(rowIndex);

				SalesLeads salesLeads = new SalesLeads();
				salesLeads.setMonth(getStringValue(row.getCell(0), validationMessage, rowIndex));
				salesLeads.setSalesName(getStringValue(row.getCell(1), validationMessage, rowIndex));
				salesLeads.setPotentialCustomer(getStringValue(row.getCell(2), validationMessage, rowIndex));
				salesLeads.setAddress(getStringValue(row.getCell(3), validationMessage, rowIndex));
				salesLeads.setPostalCode(getStringValue(row.getCell(4), validationMessage, rowIndex));
				salesLeads.setPhoneNumber(getStringValue(row.getCell(5), validationMessage, rowIndex));
				salesLeads.setEmail(getStringValue(row.getCell(6), validationMessage, rowIndex));
				salesLeads.setProduct(getProductFromCell(row.getCell(7), validationMessage, rowIndex));
				salesLeads.setProjectedValue(getNumericValue(row.getCell(8), validationMessage, rowIndex));
				salesLeads.setCurrentStage(getStringValue(row.getCell(9), validationMessage, rowIndex));
				salesLeads.setLeadsStatus(getStringValue(row.getCell(10), validationMessage, rowIndex));
				salesLeads.setNotes(getStringValue(row.getCell(11), validationMessage, rowIndex));

				// Additional validations
				if (!isValidEmail(salesLeads.getEmail())) {
					validationMessage.append(", Invalid email format in row " + (rowIndex + 1));
				}

				if (!isValidLeadsStatus(salesLeads.getLeadsStatus())) {
					validationMessage.append(", Invalid Leads Status in row " + (rowIndex + 1));
				}

				if (!isValidCurrentStage(salesLeads.getCurrentStage())) {
					validationMessage.append(", Invalid Current Stage in row " + (rowIndex + 1));
				}

				if (!isValidMonthName(salesLeads.getMonth())) {
					validationMessage.append(", Invalid month name in row " + (rowIndex + 1));
				}

				if (salesLeadsRepository
						.findByMonthAndSalesNameAndPotentialCustomerAndEmailAndPhoneNumberAndProduct_Name(
								salesLeads.getMonth(), salesLeads.getSalesName(), salesLeads.getPotentialCustomer(),
								salesLeads.getEmail(), salesLeads.getPhoneNumber(),
								salesLeads.getProduct() != null ? salesLeads.getProduct().getName() : null)
						.isPresent()) {
					validationMessage.append(", Data already exist");
				}

				excelResponse.setValidationMessage(validationMessage.toString() == "" ? "Data sudah sesuai"
						: validationMessage.toString().replaceFirst(",", ""));
				excelResponse.setSalesLeads(salesLeads);
				excelResponse.setValid(validationMessage.toString() == "" ? true : false);

				if (excelResponse.isValid()) {
					createSalesLeads(salesLeads);
				}
				customerInfoList.add(excelResponse);
			}

		}

		response.setData(customerInfoList);
		return response;
	}

	public boolean isValidMonthName(String monthName) {
		// You can implement logic to check if the month name is valid, e.g., by
		// comparing it to a list of valid month names.
		List<String> validMonthNames = Arrays.asList("January", "February", "March", "April", "May", "June", "July",
				"August", "September", "October", "November", "December");
		return validMonthNames.contains(monthName.trim());
	}

	public String getNumericValue(Cell cell, StringBuilder validationMessage, int rowIndex) {
		String value = getStringValue(cell);
		if (value == null || value.trim().isEmpty()) {
			validationMessage.append(", Projected Value in row " + (rowIndex + 1) + " is required.");
			return null;
		}

		// Validate if the value is numeric
		try {
			Double.parseDouble(value);
			return value;
		} catch (NumberFormatException e) {
			validationMessage.append(", Projected Value in row " + (rowIndex + 1) + " must be a valid number.");
			return null;
		}
	}

	public String getStringValue(Cell cell) {
		return cell == null ? null : cell.toString();
	}

	public String getStringValue(Cell cell, StringBuilder validationMessage, int rowIndex) {
		String value = getStringValue(cell);
		if (value == null || value.trim().isEmpty()) {
			validationMessage.append(", Field in row " + (rowIndex + 1) + " is required.");
		}
		return value;
	}

	public Product getProductFromCell(Cell cell, StringBuilder validationMessage, int rowIndex) {
		String productName = getStringValue(cell);
		if (productName == null || productName.trim().isEmpty()) {
			validationMessage.append(", Product name is required in row " + (rowIndex + 1));
			return null;
		}

		Optional<Product> productOptional = productRepository.findByName(productName);
		if (productOptional.isEmpty()) {
			validationMessage.append(
					", Product with name '" + productName + "' not found in the system in row " + (rowIndex + 1));
		}

		return productOptional.orElse(null);
	}

	public boolean isValidEmail(String email) {
		return email != null && EMAIL_PATTERN.matcher(email).matches();
	}

	public boolean isValidLeadsStatus(String leadsStatus) {
		return leadsStatus != null && VALID_LEADS_STATUS.contains(leadsStatus.trim());
	}

	public boolean isValidCurrentStage(String currentStage) {
		return currentStage != null && VALID_CURRENT_STAGES.contains(currentStage.trim());
	}
}
