package com.id.sales.service.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.id.sales.service.model.CustomerInfo;
import com.id.sales.service.model.InvoiceAging;

public class ExcelHelper {
	public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	static String[] HEADER_INV = { "BUP", "Tenant", "Nomer Invoice", "Tgl Invoice", "Tgl Invoice Diterima Tenant",
			"Tgl Jatuh Tempo", "Pokok Penerimaan", "Tgl Masuk Rekening Pokok", "Keterangan", "Jatuh Tempo",
			"Aging Invoice Sejak Diterima", "Denda", "Keterangan Aging Invoice", "Aging Pembayaran" };

	static String[] HEADER_CUSTOMER = { "Bulan", "BUP", "Nama Sales", "Calon Pelanggan", "Alamat", "Kelurahan",
			"Kecamatan", "Kota/Kabupaten", "Provinsi", "Kode Pos", "No Telepon", "E-mail", "Project", "Produk",
			"Proyeksi Nilai", "Aging", "Current Stage", "Leads Status", "Keterangan",

	};
	static String SHEET_CUSTOMER = "CUSTOMER";

//	private static Date formatDate(String date) {
//		SimpleDateFormat dateBefore = new SimpleDateFormat("dd/MM/yyyy");
//		Date dateR;
//		try {
//			dateR = dateBefore.parse(date);
//		} catch (ParseException e) {
//			return null;
//		}
//		return dateR;
//
//	}

	public static ByteArrayInputStream tutorialsToExcel(List<InvoiceAging> tutorials) {

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			Sheet sheet = workbook.createSheet(SHEET_CUSTOMER);
			// Header

			// Header styling
			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			font.setColor(IndexedColors.WHITE.getIndex()); // Font color white
			headerStyle.setFont(font);
			headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex()); // Background color blue
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(BorderStyle.THICK);
			headerStyle.setBorderLeft(BorderStyle.THICK);
			headerStyle.setBorderRight(BorderStyle.THICK);
			headerStyle.setBorderTop(BorderStyle.THICK);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			// Header row
			Row headerRow = sheet.createRow(0);
			headerRow.setHeight((short) 600);

			for (int col = 0; col < HEADER_INV.length; col++) {
				sheet.setColumnWidth(col, 25 * 256);
				Cell cell = headerRow.createCell(col);
				cell.setCellValue(HEADER_INV[col]);
				cell.setCellStyle(headerStyle);
			}
			int rowIdx = 1;
			for (InvoiceAging tutorial : tutorials) {
				Row row = sheet.createRow(rowIdx++);
				row.setHeight((short) 600);
				row.setHeight((short) 600);
				row.createCell(0).setCellValue(tutorial.getBup());
				row.createCell(1).setCellValue(tutorial.getTenant());
				row.createCell(2).setCellValue(tutorial.getNomerInvoice());
				row.createCell(3).setCellValue(tutorial.getTglInvoice());
				row.createCell(4).setCellValue(tutorial.getTglInvoiceDiterimaTenant());
				row.createCell(5).setCellValue(tutorial.getTglJatuhTempo());
				row.createCell(6).setCellValue(tutorial.getPokokPenerimaan());
				row.createCell(7).setCellValue(tutorial.getTglMasukRekeningPokok());
				row.createCell(8).setCellValue(tutorial.getKeterangan());
				row.createCell(9).setCellValue(tutorial.getJatuhTempo());
				row.createCell(10).setCellValue(tutorial.getAgingInvoiceSejakDiterima());
				row.createCell(11).setCellValue(tutorial.getDenda());
				row.createCell(12).setCellValue(tutorial.getKeteranganAgingInvoice());
				row.createCell(13).setCellValue(tutorial.getAgingPembayaran());

			}
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
		}
	}

	public static ByteArrayInputStream customerToExcel(List<CustomerInfo> tutorials) {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			Sheet sheet = workbook.createSheet(SHEET_CUSTOMER);
			// Header

			// Header styling
			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			font.setColor(IndexedColors.WHITE.getIndex()); // Font color white
			headerStyle.setFont(font);
			headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex()); // Background color blue
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(BorderStyle.THICK);
			headerStyle.setBorderLeft(BorderStyle.THICK);
			headerStyle.setBorderRight(BorderStyle.THICK);
			headerStyle.setBorderTop(BorderStyle.THICK);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			// Header row
			Row headerRow = sheet.createRow(0);
			headerRow.setHeight((short) 600);

			for (int col = 0; col < HEADER_CUSTOMER.length; col++) {
				sheet.setColumnWidth(col, 25 * 256);
				Cell cell = headerRow.createCell(col);
				cell.setCellValue(HEADER_CUSTOMER[col]);
				cell.setCellStyle(headerStyle);
			}
			int rowIdx = 1;
			for (CustomerInfo tutorial : tutorials) {
				Row row = sheet.createRow(rowIdx++);
				row.setHeight((short) 600);
				row.createCell(0).setCellValue(tutorial.getBulan());
				row.createCell(1).setCellValue(tutorial.getBup());
				row.createCell(2).setCellValue(tutorial.getSalesName());
				row.createCell(3).setCellValue(tutorial.getCalonPelanggan());
				row.createCell(4).setCellValue(tutorial.getAlamat());
				row.createCell(5).setCellValue(tutorial.getKelurahan());
				row.createCell(6).setCellValue(tutorial.getKecamatan());
				row.createCell(7).setCellValue(tutorial.getKabupaten());
				row.createCell(8).setCellValue(tutorial.getProvinsi());
				row.createCell(9).setCellValue(tutorial.getKodePos());
				row.createCell(10).setCellValue(tutorial.getNomerTelepon());
				row.createCell(11).setCellValue(tutorial.getEmail());
				row.createCell(12).setCellValue(tutorial.getProject());
				row.createCell(13).setCellValue(tutorial.getProduk());
				row.createCell(14).setCellValue(tutorial.getProyeksiNilai());
				row.createCell(15).setCellValue(tutorial.getCountDays());
				row.createCell(16).setCellValue(tutorial.getCurrentStage());
				row.createCell(17).setCellValue(tutorial.getLeadsStatus());
				row.createCell(18).setCellValue(tutorial.getKeterangan());

			}
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
		}
	}
}
