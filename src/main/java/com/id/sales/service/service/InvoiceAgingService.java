package com.id.sales.service.service;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.CustomerInfo;
import com.id.sales.service.model.InvoiceAging;
import com.id.sales.service.model.QInvoiceAging;
import com.id.sales.service.repository.CustomerInfoRepository;
import com.id.sales.service.repository.InvoiceAgingRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class InvoiceAgingService {

	@Autowired
	private InvoiceAgingRepository invoiceAgingRepository;

	@Autowired
	private CustomerInfoRepository customerInfoRepository;

	public Page<InvoiceAging> listSalesInfo(String tenant, String denda, String status, String bup, String search,
			Integer page, Integer size) {
		BooleanExpression predicate = QInvoiceAging.invoiceAging.id.isNotNull();
		if (bup != null) {
			predicate = predicate.and(QInvoiceAging.invoiceAging.bup.eq(bup));
		}

		if (tenant != null) {
			predicate = predicate.and(QInvoiceAging.invoiceAging.tenant.eq(tenant));
		}

		if (denda != null) {
			predicate = predicate.and(QInvoiceAging.invoiceAging.denda.eq(denda));
		}

		if (status != null) {
			predicate = predicate.and(QInvoiceAging.invoiceAging.keteranganAgingInvoice.eq(status));
		}

		if (search != null) {
			predicate = predicate.and(QInvoiceAging.invoiceAging.nomerInvoice.toUpperCase()
					.like("%" + search.toUpperCase() + "%")
					.or(QInvoiceAging.invoiceAging.tenant.toUpperCase().like("%" + search.toUpperCase() + "%"))
					.or(QInvoiceAging.invoiceAging.bup.toUpperCase().like("%" + search.toUpperCase() + "%"))
					.or(QInvoiceAging.invoiceAging.nomerInvoice.toUpperCase().like("%" + search.toUpperCase() + "%"))
					.or(QInvoiceAging.invoiceAging.pokokPenerimaan.toUpperCase().like("%" + search.toUpperCase() + "%"))
					.or(QInvoiceAging.invoiceAging.keterangan.toUpperCase().like("%" + search.toUpperCase() + "%")));
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<InvoiceAging> list = invoiceAgingRepository.findAll(predicate, pageable);
		return list;
	}

	public ResponseModel deleteINV() {
		ResponseModel model = new ResponseModel();
		invoiceAgingRepository.deleteAll();
		return model;
	}

	public ResponseModel addInvoice(InvoiceAging data) throws Exception {
		try {
			ResponseModel model = new ResponseModel();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String validationMessage = "";

			if (validationMessage != "") {
				model.setMessage(validationMessage.replaceFirst(",", ""));
				model.setSuccess("false");
				model.setStatusCode(HttpStatus.BAD_REQUEST.value());
			} else {

				// point 8
				if (data.getTglMasukRekeningPokok() != null && !data.getTglMasukRekeningPokok().equalsIgnoreCase("")) {
					if (!data.getTglJatuhTempo().equalsIgnoreCase("") && data.getTglJatuhTempo() != null) {
						if (sdf.parse(data.getTglMasukRekeningPokok()).after(sdf.parse(data.getTglJatuhTempo()))) {
							data.setDenda("Denda");
						} else {
							data.setDenda("Tidak Denda");
						}
					}

					data.setAgingInvoiceSejakDiterima("0");
				} else {
					data.setAgingInvoiceSejakDiterima(
							String.valueOf(gitDiffDay(data.getTglInvoiceDiterimaTenant(), sdf.format(new Date()))));
				}

				if (data.getTglInvoiceDiterimaTenant() != null
						&& !data.getTglInvoiceDiterimaTenant().equalsIgnoreCase("")) {
					if (data.getTglMasukRekeningPokok() != null
							&& !data.getTglMasukRekeningPokok().equalsIgnoreCase("")) {
						data.setKeteranganAgingInvoice("Sudah Bayar");
						data.setAgingPembayaran(String.valueOf(
								gitDiffDay(data.getTglInvoiceDiterimaTenant(), data.getTglMasukRekeningPokok())));
					} else {

						if (data.getTglJatuhTempo() != null && !data.getTglJatuhTempo().equalsIgnoreCase("")) {
							try {
								if (sdf.parse(data.getTglJatuhTempo()).after(new Date())) {
									data.setKeteranganAgingInvoice("Jatuh Tempo");
								}  else {
									data.setKeteranganAgingInvoice("Belum Jatuh Tempo");
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						data.setAgingPembayaran("Belum Bayar");
					}
				} else {
					data.setKeteranganAgingInvoice("Invoice belum diterima Tenant");

					data.setAgingPembayaran("Invoice belum diterima Tenant");
				}

				model.setStatusCode(HttpStatus.CREATED.value());
				invoiceAgingRepository.save(data);
			}
			return model;
		} catch (Exception e) {
			throw e;
		}
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

	private static final List<Integer> NON_BUSINESS_DAYS = Arrays.asList(Calendar.SATURDAY, Calendar.SUNDAY);

	public static Date businessDaysFrom(Date date, int businessDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		for (int i = 0; i < Math.abs(businessDays);) {
			// here, all days are added/subtracted
			calendar.add(Calendar.DAY_OF_MONTH, businessDays > 0 ? 1 : -1);

			// but at the end it goes to the correct week day.
			// because i is only increased if it is a week day
			if (!NON_BUSINESS_DAYS.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
				i++;
			}
		}
		return calendar.getTime();
	}

	public static Date notBusinessDaysFrom(Date date, int businessDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		for (int i = 0; i < Math.abs(businessDays);) {
			// here, all days are added/subtracted
			calendar.add(Calendar.DAY_OF_MONTH, businessDays > 0 ? 1 : -1);

			// but at the end it goes to the correct week day.
			// because i is only increased if it is a week day
//            if (!NON_BUSINESS_DAYS.contains(calendar.get(Calendar.DAY_OF_WEEK))){
			i++;
//            }
		}
		return calendar.getTime();
	}

	public ResponseModel editInvoice(String id, InvoiceAging data) {
		ResponseModel model = new ResponseModel();

		Optional<InvoiceAging> detailData = invoiceAgingRepository.findById(Integer.valueOf(id));

		if (detailData.isPresent()) {
			data.setId(detailData.get().getId());
			String validationMessage = "";

			if (validationMessage != "") {
				model.setMessage(validationMessage.replaceFirst(",", ""));
				model.setSuccess("false");
				model.setStatusCode(HttpStatus.BAD_REQUEST.value());
			} else {
				invoiceAgingRepository.save(data);
			}
		}

		return model;
	}

	public ResponseModel deleteInvoice(Integer id) {
		ResponseModel model = new ResponseModel();
		Optional<InvoiceAging> detailData = invoiceAgingRepository.findById(id);
		if (detailData.isPresent()) {
			invoiceAgingRepository.deleteById(id);
		} else {
			model.setMessage("Data tidak di temukan");
		}
		return model;
	}

	public ResponseModel detailInvoice(Integer id) {
		ResponseModel model = new ResponseModel();
		Optional<InvoiceAging> detailData = invoiceAgingRepository.findById(id);
		if (detailData.isPresent()) {
			model.setData(detailData.get());
		} else {
			model.setMessage("Data tidak di temukan");
		}
		return model;
	}

	public ByteArrayInputStream load() {
		List<InvoiceAging> invoice = invoiceAgingRepository.findAll();
		ByteArrayInputStream in = ExcelHelper.tutorialsToExcel(invoice);
		return in;
	}

	public ByteArrayInputStream loadCustomer() {
		List<CustomerInfo> cs = customerInfoRepository.findAll();
		ByteArrayInputStream in = ExcelHelper.customerToExcel(cs);
		return in;
	}

}
