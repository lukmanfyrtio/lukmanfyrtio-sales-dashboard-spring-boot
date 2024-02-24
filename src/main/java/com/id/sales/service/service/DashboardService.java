package com.id.sales.service.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.id.sales.service.dto.ExcelResponse;
import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.dto.SalesFunnel;
import com.id.sales.service.dto.TargetBreakdown;
import com.id.sales.service.model.CustomerInfo;
import com.id.sales.service.model.QCustomerInfo;
import com.id.sales.service.model.QSalesInfo;
import com.id.sales.service.model.SalesInfo;
import com.id.sales.service.repository.CustomerInfoRepository;
import com.id.sales.service.repository.InvoiceAgingRepository;
import com.id.sales.service.repository.SalesInfoRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class DashboardService {

	@Autowired
	private CustomerInfoRepository customerInfoRepository;

	@Autowired
	private SalesInfoRepository salesInfoRepository;

	@Autowired
	private InvoiceAgingRepository invoiceAgingRepository;

	public List<CustomerInfo> processExcelFile(MultipartFile file) {
		List<CustomerInfo> customerInfoList = new ArrayList<>();

		try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {

			Sheet sheet = workbook.getSheetAt(0); // Assuming your data is in the first sheet

			// Skip the header row by starting the iteration from the second row
			boolean isHeader = true;
			for (Row row : sheet) {
				if (isHeader) {
					isHeader = false; // This will skip the first row (header)
					continue;
				}
				// Assuming the columns are in order: BUP, Nama Sales, Calon Pelanggan, Project,
				// Produk, Proyeksi Nilai, Current Stage, Status
				String bulan = row.getCell(0).getStringCellValue();
				String bup = row.getCell(1).getStringCellValue();
				String namaSales = row.getCell(2).getStringCellValue();
				String calonPelanggan = row.getCell(3).getStringCellValue();
				String project = row.getCell(4).getStringCellValue();
				String produk = row.getCell(5).getStringCellValue();
				String proyeksiNilai = null;
				try {
					proyeksiNilai = getCellDataByCell(row.getCell(6));
				} catch (Exception e) {
					e.printStackTrace();
				}
				String currentStage = row.getCell(7).getStringCellValue();
				String status = row.getCell(8).getStringCellValue();
				String keterangan = row.getCell(9).getStringCellValue();

				// Create a CustomerInfo object
				CustomerInfo customerInfo = new CustomerInfo();
				customerInfo.setBulan(bulan);
				customerInfo.setBup(bup);
				customerInfo.setSalesName(namaSales);
				customerInfo.setCalonPelanggan(calonPelanggan);
				customerInfo.setProject(project);
				customerInfo.setProduk(produk);
				customerInfo.setProyeksiNilai(proyeksiNilai);
				customerInfo.setCurrentStage(currentStage);
				customerInfo.setLeadsStatus(status);
				customerInfo.setKeterangan(keterangan);
				// Add the CustomerInfo object to the list
				customerInfoList.add(customerInfo);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return customerInfoList;
	}

	private boolean isValidStatus(String status) {
		// Add your validation logic here
		return status.equalsIgnoreCase("Open") || status.equalsIgnoreCase("Close");
	}

	public ResponseModel excelReadTable(MultipartFile file) {
		Jwt user = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = user.getClaimAsString("username");
		ResponseModel response = new ResponseModel();
		List<ExcelResponse> customerInfoList = new ArrayList<ExcelResponse>();
		List<CustomerInfo> datas = processExcelFile(file);

		for (CustomerInfo customerInfo : datas) {
			ExcelResponse excelResponse = new ExcelResponse();
			String validationMessage = "";

			// check mandatory data
			if (customerInfo.getProject() != "" && customerInfo.getBup() != "" && customerInfo.getCalonPelanggan() != ""
					&& customerInfo.getProduk() != "" && customerInfo.getProyeksiNilai() != ""
					&& customerInfo.getBulan() != "") {
				excelResponse.setValid(true);
				excelResponse.setValidationMessage("Data sudah sesuai");
			} else {
				excelResponse.setValid(false);
				validationMessage += "Kolom BUP, Calon Pelanggan, Bulan, Calon Pelanggan, Project, Produk, Proyeksi dan Nilai mandatory";
			}

			excelResponse.setValidationMessage(
					validationMessage == "" ? "Data sudah sesuai" : validationMessage.replaceFirst(",", ""));

			SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");

			if (!isValidStatus(customerInfo.getLeadsStatus())) {
				excelResponse.setValid(false);
				validationMessage += ",Status hanya Boleh diisi dengan Open dan Close";
			}
			if (customerInfo.getCurrentStage() != null) {
				switch (customerInfo.getCurrentStage()) {
				case "Opportunities":
					if (customerInfo.getOpportunitiesOpen() == "") {
						customerInfo.setOpportunitiesOpen(DateFor.format(new Date()));
					}
					break;
				case "Proposal":
					if (customerInfo.getOpportunitiesOpen() != "") {
						customerInfo.setOpportunitiesClose(DateFor.format(new Date()));
					}
					customerInfo.setProposalOpen(DateFor.format(new Date()));
					break;
				case "Negotiation":
					if (customerInfo.getProposalOpen() == "") {
						customerInfo.setProposalClose(DateFor.format(new Date()));
					}
					customerInfo.setNegotiationOpen(DateFor.format(new Date()));
					break;
				case "Deals":
					if (customerInfo.getNegotiationOpen() == "") {
						customerInfo.setNegotiationClose(DateFor.format(new Date()));
					}
					customerInfo.setDealsOpen(DateFor.format(new Date()));
					customerInfo.setDealsClose(DateFor.format(new Date()));
					break;
				case "Dropped":
					if (customerInfo.getOpportunitiesOpen() == "") {
						customerInfo.setOpportunitiesClose(DateFor.format(new Date()));
					} else if (customerInfo.getProposalOpen() != "") {
						customerInfo.setProposalClose(DateFor.format(new Date()));
					} else if (customerInfo.getProposalOpen() == "") {
						customerInfo.setProposalClose(DateFor.format(new Date()));
					} else if (customerInfo.getNegotiationOpen() == "") {
						customerInfo.setNegotiationClose(DateFor.format(new Date()));
					}
					customerInfo.setDroppedOpen(DateFor.format(new Date()));
					customerInfo.setDroppedClose(DateFor.format(new Date()));

					break;
				default:
					excelResponse.setValid(false);
					validationMessage += ", Kolom Current Stage hanya boleh diisi dengan Opportunities, Proposal, Negotiation, Deals dan Dropped";
					break;
				}
				customerInfo.setCreatedBy(username);
			}
			if (excelResponse.isValid()) {
				customerInfo.setCreatedTime(new Date());
				addCustomer(customerInfo, username);
			} else {
				excelResponse.setValidationMessage(validationMessage.replaceFirst(",", ""));
			}

			excelResponse.setCustomerInfo(customerInfo);
			customerInfoList.add(excelResponse);

		}

		response.setData(customerInfoList);
		return response;
	}

	public String getCellData(XSSFSheet sheet, String cellId, FormulaEvaluator formulaEvaluator) throws Exception {
		DataFormatter dataFormatter = new DataFormatter();
		CellAddress cellAddress = new CellAddress(cellId);
		Row row = sheet.getRow(cellAddress.getRow());
		Cell cell = row.getCell(cellAddress.getColumn());
		try {
			String CellData = null;
			if (cell.getCellType() == CellType.FORMULA) {
				switch (cell.getCachedFormulaResultType()) {
				case BOOLEAN:
					CellData = String.valueOf(cell.getBooleanCellValue());
					break;
				case NUMERIC:
					if (formulaEvaluator != null) {
						CellData = String.valueOf(dataFormatter.formatCellValue(cell, formulaEvaluator));
					} else {
						CellData = String.valueOf((long) cell.getNumericCellValue());
					}
					break;
				case STRING:
					CellData = String.valueOf(cell.getRichStringCellValue());
					break;
				default:
					break;
				}
			} else {
				switch (cell.getCellType()) {
				case STRING:
					CellData = cell.getStringCellValue();
					break;
				case NUMERIC:
					if (formulaEvaluator != null) {
						CellData = String.valueOf(dataFormatter.formatCellValue(cell, formulaEvaluator));
					} else {
						CellData = String.valueOf((long) cell.getNumericCellValue());
					}
					break;
				case BOOLEAN:
					CellData = Boolean.toString(cell.getBooleanCellValue());
					break;
				case BLANK:
					CellData = "";
					break;
				default:
					break;
				}
			}
			return CellData;
		} catch (Exception e) {
			return "";
		}
	}

	public String getCellDataByCell(Cell cell) throws Exception {
//		DataFormatter dataFormatter = new DataFormatter();
		try {
			String CellData = null;
			if (cell.getCellType() == CellType.FORMULA) {
				switch (cell.getCachedFormulaResultType()) {
				case BOOLEAN:
					CellData = String.valueOf(cell.getBooleanCellValue());
					break;
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						CellData = String.valueOf(cell.getDateCellValue());
					} else {
						CellData = String.valueOf((long) cell.getNumericCellValue());
					}
					break;
				case STRING:
					CellData = String.valueOf(cell.getRichStringCellValue());
					break;
				default:
					break;
				}
			} else {
				switch (cell.getCellType()) {
				case STRING:
					CellData = cell.getStringCellValue();
					break;
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						CellData = String.valueOf(cell.getDateCellValue());
					} else {
						CellData = String.valueOf((long) cell.getNumericCellValue());
					}
					break;
				case BOOLEAN:
					CellData = Boolean.toString(cell.getBooleanCellValue());
					break;
				case BLANK:
					CellData = "";
					break;
				default:
					break;
				}
			}
			return CellData;
		} catch (Exception e) {
			return "";
		}
	}

//	private String formatToIdr(Double idr) {
//		DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
//		kursIndonesia.setDecimalSeparatorAlwaysShown(false);
//		kursIndonesia.setMaximumFractionDigits(0);
//		DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
//
//		formatRp.setCurrencySymbol("Rp. ");
//		formatRp.setGroupingSeparator('.');
//
//		kursIndonesia.setDecimalFormatSymbols(formatRp);
//		return kursIndonesia.format(idr);
//	}

	public Page<CustomerInfo> listCustomerInfo(String stage, String status, String bup, String search, Integer page,
			Integer size) {
		BooleanExpression predicate = QCustomerInfo.customerInfo.idPelanggan.isNotNull();
		if (bup != null) {
			predicate = predicate.and(QCustomerInfo.customerInfo.bup.eq(bup));
		}

		if (stage != null) {
			predicate = predicate.and(QCustomerInfo.customerInfo.currentStage.eq(stage));
		}

		if (status != null) {
			predicate = predicate.and(QCustomerInfo.customerInfo.leadsStatus.eq(status));
		}

		if (search != null) {
			predicate = predicate.and(QCustomerInfo.customerInfo.salesName.toUpperCase()
					.like("%" + search.toUpperCase() + "%").or(QCustomerInfo.customerInfo.calonPelanggan.toUpperCase()
							.like("%" + search.toUpperCase() + "%")));
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<CustomerInfo> list = customerInfoRepository.findAll(predicate, pageable);
		return list;

	}

	public ResponseModel addCustomer(CustomerInfo data, String createdBy) {
		ResponseModel model = new ResponseModel();
		String validationMessage = "";
		// check mandatory data
		if (data.getProject() == "" && data.getBup() == "" && data.getCalonPelanggan() == "" && data.getProduk() == ""
				&& data.getCurrentStage() == "" && data.getLeadsStatus() == "" && data.getSalesName() == ""
				&& data.getProyeksiNilai() == "") {
			validationMessage += "Kolom BUP, Nama Sales, Calon Pelanggan, Project, Produk, Proyeksi Nilai, Current Stage dan Status mandatory";
		}

		SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");
		if (data.getCurrentStage() != null) {
			switch (data.getCurrentStage()) {
			case "Opportunities":
				if (data.getOpportunitiesOpen() == null || data.getOpportunitiesOpen() == "") {
					data.setOpportunitiesOpen(DateFor.format(new Date()));
				}
				break;
			case "Proposal":
				if (data.getOpportunitiesOpen() == null || data.getOpportunitiesOpen() != "") {
					data.setOpportunitiesClose(DateFor.format(new Date()));
				}
				data.setProposalOpen(DateFor.format(new Date()));
				break;
			case "Negotiation":
				if (data.getProposalOpen() == null || data.getProposalOpen() == "") {
					data.setProposalClose(DateFor.format(new Date()));
				}
				data.setNegotiationOpen(DateFor.format(new Date()));
				break;
			case "Deals":
				if (data.getNegotiationOpen() == null || data.getNegotiationOpen() == "") {
					data.setNegotiationClose(DateFor.format(new Date()));
				}
				data.setDealsOpen(DateFor.format(new Date()));
				data.setDealsClose(DateFor.format(new Date()));
				break;
			case "Dropped":
				if (data.getOpportunitiesOpen() == null || data.getOpportunitiesOpen() == "") {
					data.setOpportunitiesClose(DateFor.format(new Date()));
				} else if (data.getProposalOpen() == null || data.getProposalOpen() != "") {
					data.setProposalClose(DateFor.format(new Date()));
				} else if (data.getProposalOpen() == null || data.getProposalOpen() == "") {
					data.setProposalClose(DateFor.format(new Date()));
				} else if (data.getNegotiationOpen() == null || data.getNegotiationOpen() == "") {
					data.setNegotiationClose(DateFor.format(new Date()));
				}
				data.setDroppedOpen(DateFor.format(new Date()));
				data.setDroppedClose(DateFor.format(new Date()));

				break;
			default:
				break;
			}
			data.setCreatedTime(new Date());
			data.setCreatedBy(createdBy);
			if (validationMessage != "") {
				model.setMessage(validationMessage.replaceFirst(",", ""));
				model.setSuccess("false");
				model.setStatusCode(HttpStatus.BAD_REQUEST.value());
			} else {
				model.setStatusCode(HttpStatus.CREATED.value());
				customerInfoRepository.save(data);
			}

		} else {
			model.setMessage("Current stage is mandatory");
		}
		return model;
	}

	public ResponseModel editCustomer(String id, CustomerInfo data, String updateBy) {
		ResponseModel model = new ResponseModel();

		Optional<CustomerInfo> detailData = customerInfoRepository.findById(Long.valueOf(id));

		if (detailData.isPresent()) {
			data.setIdPelanggan(detailData.get().getIdPelanggan());
			String validationMessage = "";
			// check mandatory data
			if (data.getProject() != "" && data.getBup() != "" && data.getCalonPelanggan() != ""
					&& data.getProduk() != "" && data.getCurrentStage() != "" && data.getLeadsStatus() != ""
					&& data.getSalesName() != "" && data.getProyeksiNilai() != "") {
			} else {
				validationMessage += ", Kolom BUP,Nama Sales,Calon Pelanggan,Project,Produk,Proyeksi Nilai,Current Stage, dan Status mandatory";
			}
			SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");
			if (data.getCurrentStage() != null) {
				switch (data.getCurrentStage()) {
				case "Opportunities":
					if (detailData.get().getOpportunitiesOpen() == ""
							|| detailData.get().getOpportunitiesOpen() == null) {
						data.setOpportunitiesOpen(DateFor.format(new Date()));
					}
					break;
				case "Proposal":
					if (detailData.get().getOpportunitiesOpen() != ""
							|| detailData.get().getOpportunitiesOpen() != null) {
						data.setOpportunitiesClose(DateFor.format(new Date()));
					}

					if (detailData.get().getProposalOpen() != "" || detailData.get().getProposalOpen() != null) {
						data.setProposalOpen(DateFor.format(new Date()));
					}
					break;
				case "Negotiation":
					if (detailData.get().getProposalOpen() != "" || detailData.get().getProposalOpen() != null) {
						data.setProposalClose(DateFor.format(new Date()));
					}

					if (detailData.get().getNegotiationOpen() != "" || detailData.get().getNegotiationOpen() != null) {
						data.setNegotiationOpen(DateFor.format(new Date()));
					}
					break;
				case "Deals":
					if (detailData.get().getNegotiationOpen() != "" || detailData.get().getNegotiationOpen() != null) {
						data.setNegotiationClose(DateFor.format(new Date()));
					}
					if (detailData.get().getDealsOpen() != "" || detailData.get().getDealsOpen() != null) {
						data.setDealsOpen(DateFor.format(new Date()));
					}

					if (detailData.get().getDealsClose() != "" || detailData.get().getDealsClose() != null) {
						data.setDealsClose(DateFor.format(new Date()));
					}
					data.setLeadsStatus("Close");
					break;
				case "Dropped":
					if (detailData.get().getOpportunitiesOpen() != ""
							|| detailData.get().getOpportunitiesOpen() != null) {
						data.setOpportunitiesClose(DateFor.format(new Date()));
					} else if (detailData.get().getProposalOpen() != "" || detailData.get().getProposalOpen() != null) {
						data.setProposalClose(DateFor.format(new Date()));
					} else if (detailData.get().getProposalOpen() != "" || detailData.get().getProposalOpen() != null) {
						data.setProposalClose(DateFor.format(new Date()));
					} else if (detailData.get().getNegotiationOpen() != ""
							|| detailData.get().getNegotiationOpen() != null) {
						data.setNegotiationClose(DateFor.format(new Date()));
					}
					if (detailData.get().getDroppedOpen() != "" || detailData.get().getDroppedOpen() != null) {
						data.setDroppedOpen(DateFor.format(new Date()));
					}
					if (detailData.get().getDroppedClose() != "" || detailData.get().getDroppedClose() != null) {
						data.setDroppedClose(DateFor.format(new Date()));
					}
					data.setLeadsStatus("Close");

					break;
				default:
					break;
				}
				data.setCreatedBy(detailData.get().getCreatedBy());
				data.setCreatedTime(detailData.get().getCreatedTime());
				data.setUpdatedTime(new Date());
				data.setUpdatedBy(updateBy);
				if (validationMessage != "") {
					model.setMessage(validationMessage.replaceFirst(",", ""));
					model.setSuccess("false");
					model.setStatusCode(HttpStatus.BAD_REQUEST.value());
				} else {
					model.setStatusCode(HttpStatus.CREATED.value());
					customerInfoRepository.save(data);
				}

			} else {
				model.setMessage("Current stage is mandatory");
			}
			if (validationMessage != "") {
				model.setMessage(validationMessage.replaceFirst(",", ""));
				model.setSuccess("false");
				model.setStatusCode(HttpStatus.BAD_REQUEST.value());
			} else {
				customerInfoRepository.save(data);
			}

		}
		return model;
	}

//	private String formatDate(String inputDate) {
//		try {
//			SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");
//
//			SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
//			if (inputDate == null) {
//				return "";
//			}
//			return DateFor.format(formatter.parse(inputDate));
//		} catch (ParseException e) {
//			return "";
//		}
//	}

	public ResponseModel deleteCustomer(Long id) {
		ResponseModel model = new ResponseModel();
		Optional<CustomerInfo> detailData = customerInfoRepository.findById(id);
		if (detailData.isPresent()) {
			customerInfoRepository.deleteById(id);
		} else {
			model.setMessage("Data tidak di temukan");
		}
		return model;
	}

	public ResponseModel detailCustomer(Long id) {
		ResponseModel model = new ResponseModel();
		Optional<CustomerInfo> detailData = customerInfoRepository.findById(id);
		if (detailData.isPresent()) {
			model.setData(detailData.get());
		} else {
			model.setMessage("Data tidak di temukan");
		}
		return model;
	}

	public ResponseModel deleteCustomer() {
		ResponseModel model = new ResponseModel();
		customerInfoRepository.deleteAll();
		return model;
	}

	public ResponseModel getTargetBreakdown(String tahun, String bup) {
		ResponseModel model = new ResponseModel();

		model.setData(getTarget(tahun, bup));
		return model;
	}

	private TargetBreakdown getTarget(String tahun, String bup) {
		TargetBreakdown tg = new TargetBreakdown();
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");

		BooleanExpression expression = QSalesInfo.salesInfo.id.isNotNull();
		expression = expression.and(QSalesInfo.salesInfo.tahun.eq(String.valueOf(tahun)));
		if (bup != null) {
			expression = expression.and(QSalesInfo.salesInfo.bup.eq(bup));
		}
		List<SalesInfo> salesInfo = StreamSupport.stream(salesInfoRepository.findAll(expression).spliterator(), true)
				.collect(Collectors.toList());

		String bupTahun = bup != null ? tahun + bup : tahun;

		// existingCustomer
		Double existing = salesInfo.stream().filter(
				data -> bupTahun.equalsIgnoreCase(bup != null ? data.getTahun() + data.getBup() : data.getTahun()))
				.mapToDouble(x -> Double.valueOf(x.getExisting().doubleValue())).sum();

		// sales funnel
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		Iterable<CustomerInfo> prospekList = customerInfoRepository.getListByBUPAndYearIsNotNull(bup,
				Integer.valueOf(tahun));
		Double prospekCount = StreamSupport.stream(prospekList.spliterator(), false)
				.mapToDouble(x -> Double.valueOf(
						x.getProyeksiNilai().isEmpty() || x.getProyeksiNilai().isBlank() ? "0" : x.getProyeksiNilai()))
				.sum() / 1000000000.0;

		SimpleDateFormat date = new SimpleDateFormat("MMMM");
		String bulanNow = date.format(new Date());

		double mtdTarget = 0;
		double mtdcashIn = 0;
		double gap = 0;
		for (String month : months) {
			SalesInfo salesData = new SalesInfo();
			if (salesInfo.isEmpty()) {
				salesData.setExisting(BigDecimal.ZERO);
				salesData.setTarget(BigDecimal.ZERO);
			} else {
				salesData = salesInfo.stream().filter(salesInfoData -> salesInfoData.getBulan().equalsIgnoreCase(month))
						.findFirst().get();
			}

			Double cashInPerMonth = StreamSupport.stream(prospekList.spliterator(), false)
					.filter(c -> month.equalsIgnoreCase(c.getBulan()))
					.mapToDouble(
							x -> Double.valueOf(x.getProyeksiNilai().isEmpty() || x.getProyeksiNilai().isBlank() ? "0"
									: x.getProyeksiNilai()))
					.sum() / 1000000000.0;

			BigDecimal pPokok = invoiceAgingRepository.getPenerimaanPokokByBulanBUPYear(month, Integer.valueOf(tahun),
					bup);
			System.out.println(month + cashInPerMonth);
			mtdTarget = Double.sum(mtdTarget, salesData.getTarget().doubleValue());
			String toBn=toBn(nvl(pPokok));
			mtdcashIn = Double.sum(mtdcashIn, Double.valueOf(toBn));
			System.out.println("bulan = " + month + " cash in =" + pPokok);
			if (month.equalsIgnoreCase(bulanNow)) {
				break;
			}
		}

		for (String month : months) {
			SalesInfo salesData = new SalesInfo();
			if (salesInfo.isEmpty()) {
				salesData.setExisting(BigDecimal.ZERO);
				salesData.setTarget(BigDecimal.ZERO);
			} else {
				salesData = salesInfo.stream().filter(salesInfoData -> salesInfoData.getBulan().equalsIgnoreCase(month))
						.findFirst().get();
			}

			Double existingPermonth = salesData.getExisting().doubleValue();
			Double targetPerMonth = salesData.getTarget().doubleValue();
			Double prospekPerMonth = StreamSupport.stream(prospekList.spliterator(), false)
					.filter(c -> month.equalsIgnoreCase(c.getBulan()))
					.mapToDouble(
							x -> Double.valueOf(x.getProyeksiNilai().isEmpty() || x.getProyeksiNilai().isBlank() ? "0"
									: x.getProyeksiNilai()))
					.sum() / 1000000000.0;
			gap = Double.sum(gap, Double.valueOf(targetPerMonth - existingPermonth - prospekPerMonth));
			System.out.println(String.format("targetPerMonth %f - existingPermonth %f  - cashInPerMonth %f = %f",
					targetPerMonth, existingPermonth, prospekPerMonth,
					Double.valueOf(targetPerMonth - existingPermonth - prospekPerMonth)));
		}

		DecimalFormat dfP = new DecimalFormat();
		dfP.setMaximumFractionDigits(2);

		tg.setAchievement(dfP.format(Double.valueOf((mtdcashIn / mtdTarget) * 100)));
		tg.setCashInTarget(df.format(existing + prospekCount + gap));
		tg.setExistingCustomer(df.format(existing));
		tg.setGAP(df.format(gap));
		tg.setMtdCashIn(df.format(mtdcashIn));
		tg.setMtdTarget(df.format(mtdTarget));
		tg.setSalesFunnel(df.format(prospekCount));
		return tg;
	}

	public ResponseModel getTargetBreakdownALL(String tahun) {
		DecimalFormat dfP = new DecimalFormat();
		dfP.setMaximumFractionDigits(2);
		ResponseModel model = new ResponseModel();
		TargetBreakdown result = new TargetBreakdown();
		TargetBreakdown resultPTT = getTarget(tahun, "SWAMEDIA");
		TargetBreakdown resultLTI = getTarget(tahun, "MOTIO");
		TargetBreakdown resultPRB = getTarget(tahun, "SWADAMA");

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		result.setSalesFunnel(df.format(Double.valueOf(Double.valueOf(resultLTI.getSalesFunnel())
				+ Double.valueOf(resultPRB.getSalesFunnel()) + Double.valueOf(resultPTT.getSalesFunnel()))));

		result.setCashInTarget(df.format(Double.valueOf(resultLTI.getCashInTarget())
				+ Double.valueOf(resultPRB.getCashInTarget()) + Double.valueOf(resultPTT.getCashInTarget())));

		result.setExistingCustomer(df.format(Double.valueOf(Double.valueOf(resultLTI.getExistingCustomer())
				+ Double.valueOf(resultPRB.getExistingCustomer()) + Double.valueOf(resultPTT.getExistingCustomer()))));

		result.setGAP(df.format(Double.valueOf(resultLTI.getGAP()) + Double.valueOf(resultPRB.getGAP())
				+ Double.valueOf(resultPTT.getGAP())));

		result.setMtdCashIn(df.format(Double.valueOf(resultLTI.getMtdCashIn())
				+ Double.valueOf(resultPRB.getMtdCashIn()) + Double.valueOf(resultPTT.getMtdCashIn())));

		result.setMtdTarget(df.format(Double.valueOf(resultLTI.getMtdTarget())
				+ Double.valueOf(resultPRB.getMtdTarget()) + Double.valueOf(resultPTT.getMtdTarget())));

		result.setAchievement(
				dfP.format(Double.valueOf(result.getMtdCashIn()) / Double.valueOf(result.getMtdTarget()) * 100));
		model.setData(result);
		return model;
	}

	public ResponseModel getTargetVsActual(String tahun, String bup) {
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");
		ResponseModel model = new ResponseModel();

		List<String> actualList = new ArrayList<String>();
		List<String> targetList = new ArrayList<String>();

		BooleanExpression expression = QSalesInfo.salesInfo.id.isNotNull();
		expression = expression.and(QSalesInfo.salesInfo.tahun.eq(tahun));
		expression = expression.and(QSalesInfo.salesInfo.bup.eq(bup));
		List<SalesInfo> salesInfo = StreamSupport.stream(salesInfoRepository.findAll(expression).spliterator(), true)
				.collect(Collectors.toList());

		months.forEach(month -> {
			try {
				SalesInfo dataS = StreamSupport.stream(salesInfo.spliterator(), false)
						.filter(sales -> sales.getBulan().equalsIgnoreCase(month)).findFirst().get();
				targetList.add(dataS.getTarget().toString());
				System.out.println(dataS.getTarget());
			} catch (Exception e) {
				targetList.add("0");
			}
			try {
				BigDecimal total = nvl(
						invoiceAgingRepository.getPenerimaanPokokByBulanBUPYear(month, Integer.valueOf(tahun), bup));
				actualList.add(toBn(total));
			} catch (Exception e) {
				actualList.add("0");
			}

		});
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("actual", actualList);
		result.put("target", targetList);
		model.setData(result);
		return model;
	}

	public ResponseModel getExistGAP(String tahun, String bup) {
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");
		// sales funnel
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		Iterable<CustomerInfo> prospekList = customerInfoRepository.getListByBUPAndYearIsNotNull(bup,
				Integer.valueOf(tahun));
		ResponseModel model = new ResponseModel();

		List<String> existing = new ArrayList<String>();
		List<String> gap = new ArrayList<String>();
		List<String> prospekS = new ArrayList<String>();

		BooleanExpression expression = QSalesInfo.salesInfo.id.isNotNull();
		expression = expression.and(QSalesInfo.salesInfo.tahun.eq(tahun));
		expression = expression.and(QSalesInfo.salesInfo.bup.eq(bup));
		List<SalesInfo> salesInfo = StreamSupport.stream(salesInfoRepository.findAll(expression).spliterator(), true)
				.collect(Collectors.toList());

		double lastProspek = 0;
		for (String month : months) {
			SalesInfo salesData = new SalesInfo();
			if (salesInfo.isEmpty()) {
				salesData.setExisting(BigDecimal.ZERO);
				salesData.setTarget(BigDecimal.ZERO);
			} else {
				salesData = salesInfo.stream().filter(salesInfoData -> salesInfoData.getBulan().equalsIgnoreCase(month))
						.findFirst().get();
			}

			Double existingPermonth = salesData.getExisting().doubleValue();
			Double targetPerMonth = salesData.getTarget().doubleValue();
			Double prospekPerMonth = StreamSupport.stream(prospekList.spliterator(), false)
					.filter(c -> month.equalsIgnoreCase(c.getBulan()))
					.mapToDouble(
							x -> Double.valueOf(x.getProyeksiNilai().isEmpty() || x.getProyeksiNilai().isBlank() ? "0"
									: x.getProyeksiNilai()))
					.sum() / 1000000000.0;
			existing.add(df.format(existingPermonth));

//			prospekS.add(df.format(prospekPerMonth));

			System.out.println("prospekPM=" + prospekPerMonth);
			if (prospekPerMonth != 0.0) {
				lastProspek += prospekPerMonth / (12 - (months.indexOf(month) + 1));
				System.out.println("lastP=" + lastProspek);
			}
			prospekS.add(df.format(lastProspek));
			gap.add(df.format(Double.valueOf(targetPerMonth - existingPermonth - lastProspek)));

		}
		;
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("existing", existing);
		result.put("gap", gap);
		result.put("prospek", prospekS);
		model.setData(result);
		return model;
	}

	public ResponseModel getAvgSales(String bup, String tahun) {
		ResponseModel model = new ResponseModel();
		List<Long> op = new ArrayList<Long>();
		List<Long> pr = new ArrayList<Long>();
		List<Long> ng = new ArrayList<Long>();
		List<Long> dl = new ArrayList<Long>();
		List<CustomerInfo> custInfo = customerInfoRepository.getListByBUPAndYear(bup, Integer.valueOf(tahun));
		custInfo.forEach(cust -> {
			SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");

			if (cust.getCurrentStage().equalsIgnoreCase("Opportunities")) {
				long diffDay = gitDiffDay(cust.getOpportunitiesOpen(),
						cust.getProposalOpen() != null ? cust.getProposalOpen() : myFormat.format(new Date()));
				op.add(diffDay);

			} else if (cust.getCurrentStage().equalsIgnoreCase("Proposal")) {
				long diffDay = gitDiffDay(cust.getProposalOpen(),
						cust.getNegotiationOpen() != null ? cust.getNegotiationOpen() : myFormat.format(new Date()));
				pr.add(diffDay);

			} else if (cust.getCurrentStage().equalsIgnoreCase("Negotiation")) {
				long diffDay = gitDiffDay(cust.getNegotiationOpen(),
						cust.getDealsOpen() != null ? cust.getDealsOpen() : myFormat.format(new Date()));
				ng.add(diffDay);

			} else if (cust.getCurrentStage().equalsIgnoreCase("Deals")) {
				long diffDay = gitDiffDay(cust.getOpportunitiesOpen(),
						cust.getDealsOpen() != null ? cust.getDealsOpen() : myFormat.format(new Date()));
				dl.add(diffDay);
			}
		});

		Map<String, Object> resMap = new HashMap<>();
		resMap.put("opportunities", Math.round(calculateAverage(op)));
		resMap.put("negotiation", Math.round(calculateAverage(ng)));
		resMap.put("deals", Math.round(calculateAverage(dl)));
		resMap.put("proposal", Math.round(calculateAverage(pr)));
		model.setData(resMap);
		return model;
	}

	public ResponseModel getDataSales(String bup, String tahun, Integer page, Integer size) {
		ResponseModel model = new ResponseModel();

		List<CustomerInfo> custInfo = customerInfoRepository.getListByBUPAndYear(bup, Integer.valueOf(tahun));

		List<CustomerInfo> listDistictSales = StreamSupport.stream(custInfo.spliterator(), false)
				.filter(distinctByKey(p -> p.getSalesName())).collect(Collectors.toList());

		List<Map<String, Object>> salesResInfo = new ArrayList<Map<String, Object>>();
		listDistictSales.forEach(sales -> {
			List<Long> avgSalesC = new ArrayList<Long>();

			custInfo.forEach(cust -> {
				if (cust.getSalesName().equals(sales.getSalesName())) {
					if (!cust.getCurrentStage().equalsIgnoreCase("Dropped")) {
						SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
						long diffDay = gitDiffDay(cust.getOpportunitiesOpen(),
								cust.getDealsOpen() != null ? cust.getDealsOpen() : myFormat.format(new Date()));
						avgSalesC.add(diffDay);
						System.out.println(
								"diff day =" + diffDay + " bup=" + cust.getBup() + "sales =" + sales.getSalesName());
					}
				}
			});

			long droppedCount = customerInfoRepository.getListByStageBUPAndYearSalesName(bup, Integer.valueOf(tahun),
					"Dropped", "Close", sales.getSalesName()).size();
			long dealsCount = customerInfoRepository.getListByStageBUPAndYearSalesName(bup, Integer.valueOf(tahun),
					"Deals", "Close", sales.getSalesName()).size();
			long negotiationCount = customerInfoRepository.getListByStageBUPAndYearSalesName(bup,
					Integer.valueOf(tahun), "Negotiation", "Open", sales.getSalesName()).size();
			long proposalCount = customerInfoRepository.getListByStageBUPAndYearSalesName(bup, Integer.valueOf(tahun),
					"Proposal", "Open", sales.getSalesName()).size();
			long opportunitiesCount = customerInfoRepository.getListByStageBUPAndYearSalesName(bup,
					Integer.valueOf(tahun), "Opportunities", "Open", sales.getSalesName()).size();
			double avgSales = calculateAverage(avgSalesC);
			String leadConvertionRate = "0";
			try {
				// count leads converstion rate
				long sumAll = opportunitiesCount + proposalCount + negotiationCount + dealsCount + droppedCount;
				leadConvertionRate = String.valueOf(Math.round((double) dealsCount / (double) sumAll * 100));
			} catch (ArithmeticException e) {
				System.out.println("We are just printing the stack trace.\n"
						+ "ArithmeticException is handled. But take care of the variable \"leadConvertionRate\"");
			}
			System.out.println("avgSalesCycle= " + calculateAverage(avgSalesC));
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("opportunities", format(opportunitiesCount));
			resMap.put("negotiation", format(negotiationCount));
			resMap.put("deals", format(dealsCount));
			resMap.put("dropped", format(droppedCount));
			resMap.put("proposal", format(proposalCount));
			resMap.put("leadConvertionRate", leadConvertionRate);
			resMap.put("avgSales", Math.round(avgSales));
			resMap.put("salesName", sales.getSalesName());
			salesResInfo.add(resMap);
		});
		Pageable pageable = PageRequest.of(page, size);
		int start = Math.min((int) pageable.getOffset(), salesResInfo.size());
		int end = Math.min((start + pageable.getPageSize()), salesResInfo.size());
		Page<Map<String, Object>> pages = new PageImpl<Map<String, Object>>(salesResInfo.subList(start, end), pageable,
				salesResInfo.size());
		model.setData(pages);
		return model;
	}

	public ResponseModel getDetailDashboard(String bup, String tahun) {
		ResponseModel model = new ResponseModel();

		List<CustomerInfo> custInfo = customerInfoRepository.getListByBUPAndYear(bup, Integer.valueOf(tahun));

		List<Long> avgSalesC = new ArrayList<Long>();
		SimpleDateFormat myFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		custInfo.forEach(cust -> {
			if (cust.getCurrentStage().equalsIgnoreCase("Deals")) {
				SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
				long diffDay = gitDiffDay(cust.getOpportunitiesOpen(),
						cust.getDealsOpen() != null ? cust.getDealsOpen() : myFormat.format(new Date()));
				avgSalesC.add(diffDay);
				System.out.println("diff day =" + diffDay + " bup=" + cust.getBup());
			}
		});
		double avgSales = calculateAverage(avgSalesC);
		System.out.println("avgSalesCycle= " + calculateAverage(avgSalesC));

		long droppedCount = customerInfoRepository
				.getListByStageBUPAndYear(bup, Integer.valueOf(tahun), "Dropped", "Close").size();
		long dealsCount = customerInfoRepository.getListByStageBUPAndYear(bup, Integer.valueOf(tahun), "Deals", "Close")
				.size();
		long negotiationCount = customerInfoRepository
				.getListByStageBUPAndYear(bup, Integer.valueOf(tahun), "Negotiation", "Open").size();
		long proposalCount = customerInfoRepository
				.getListByStageBUPAndYear(bup, Integer.valueOf(tahun), "Proposal", "Open").size();
		long opportunitiesCount = customerInfoRepository
				.getListByStageBUPAndYear(bup, Integer.valueOf(tahun), "Opportunities", "Open").size();
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		SalesFunnel salesFunnel = new SalesFunnel();
		salesFunnel.setDeals(String.valueOf(dealsCount));
		salesFunnel.setDropped(String.valueOf(droppedCount));
		salesFunnel.setNegotiation(String.valueOf(negotiationCount));
		salesFunnel.setProposal(String.valueOf(proposalCount));
		salesFunnel.setOpportunities(String.valueOf(opportunitiesCount));
		salesFunnel.setAvgSalesCycle(String.valueOf(Math.round(avgSales)));
		Date lstUpd = customerInfoRepository.getLastUpdate(bup, Integer.valueOf(tahun));
		Date createdDate = customerInfoRepository.getLastCreated(bup, Integer.valueOf(tahun));
		Date lstUpdCreated = lstUpd == null ? createdDate
				: createdDate.getTime() > lstUpd.getTime() ? createdDate : lstUpd;
		salesFunnel.setLastUpdated(lstUpdCreated != null ? myFormat1.format(lstUpdCreated) : "-");
		String leadConvertionRate = "0";
		try {
			// count leads converstion rate
			long sumAll = opportunitiesCount + proposalCount + negotiationCount + dealsCount + droppedCount;
			leadConvertionRate = String.valueOf(Math.round((double) dealsCount / (double) sumAll * 100));
		} catch (ArithmeticException e) {
			System.out.println("We are just printing the stack trace.\n"
					+ "ArithmeticException is handled. But take care of the variable \"leadConvertionRate\"");
		}
		salesFunnel.setLeadsConvertionRate(leadConvertionRate);
		model.setData(salesFunnel);
		return model;
	}

	private double calculateAverage(List<Long> marks) {
		return marks.stream().mapToDouble(d -> d).average().orElse(0.0);
	}

	private long gitDiffDay(String inputString1, String inputString2) {
		SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");

		try {
			Date date1 = myFormat.parse(inputString1);
			Date date2 = myFormat.parse(inputString2.equalsIgnoreCase("") ? myFormat.format(new Date()) : inputString2);
			long diff = date2.getTime() - date1.getTime();
			return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static BigDecimal nvl(final BigDecimal bigDecimal) {
 		return bigDecimal == null ? BigDecimal.ZERO : bigDecimal;
	}

	public static String toBn(final BigDecimal bigDecimal) {
        BigDecimal billion =bigDecimal.divide(new BigDecimal("1000000000"));
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return decimalFormat.format(billion);
	}

	public static String format(final long number) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		return df.format(number);
	}

	public static String format(final double number) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		return df.format(number);
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

}
