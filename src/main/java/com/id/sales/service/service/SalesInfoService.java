package com.id.sales.service.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.id.sales.service.dto.AddRequestTarget;
import com.id.sales.service.dto.DetailResponse;
import com.id.sales.service.dto.MonthClass;
import com.id.sales.service.dto.MonthName;
import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.QSalesInfo;
import com.id.sales.service.model.SalesInfo;
import com.id.sales.service.repository.SalesInfoRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class SalesInfoService {
	@Autowired
	private SalesInfoRepository salesInfoRepository;

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	public Page<SalesInfo> listSalesInfo(String bulan, String tahun, String bup, String search, Integer page,
			Integer size) {
		BooleanExpression predicate = QSalesInfo.salesInfo.id.isNotNull();
		if (bup != null) {
			predicate = predicate.and(QSalesInfo.salesInfo.bup.eq(bup));
		}

		if (bulan != null) {
			predicate = predicate.and(QSalesInfo.salesInfo.bulan.eq(bulan));
		}

		if (tahun != null) {
			predicate = predicate.and(QSalesInfo.salesInfo.tahun.eq(tahun));
		}

		if (search != null) {
			predicate = predicate.and(QSalesInfo.salesInfo.bulan.toUpperCase().like("%" + search.toUpperCase() + "%")
					.or(QSalesInfo.salesInfo.tahun.toUpperCase().like("%" + search.toUpperCase() + "%")));
		}

		Pageable pageable = PageRequest.of(page, size);
		Iterable<SalesInfo> listI = salesInfoRepository.findAll(predicate);
		List<SalesInfo> list = StreamSupport.stream(listI.spliterator(), false).collect(Collectors.toList());

		List<SalesInfo> listDistict = list.stream().filter(distinctByKey(p -> p.getTahun() + p.getBup()))
				.collect(Collectors.toList());

		listDistict.forEach(dataD -> {
			String bupTahun = dataD.getTahun() + dataD.getBup();
			Double existing = list.stream().filter(data -> bupTahun.equalsIgnoreCase(data.getTahun() + data.getBup()))
					.mapToDouble(x -> Double.valueOf(x.getExisting().doubleValue())).sum();
			Double target = list.stream().filter(data -> bupTahun.equalsIgnoreCase(data.getTahun() + data.getBup()))
					.mapToDouble(x -> Double.valueOf(x.getTarget().doubleValue())).sum();
			DecimalFormat formatter = new DecimalFormat("#0.00");
			dataD.setExisting(new BigDecimal(formatter.format(existing)));
			dataD.setTarget(new BigDecimal(formatter.format(target)));
		});

		int start = Math.min((int) pageable.getOffset(), listDistict.size());
		int end = Math.min((start + pageable.getPageSize()), listDistict.size());
		Page<SalesInfo> pages = new PageImpl<SalesInfo>(listDistict.subList(start, end), pageable, listDistict.size());
		return pages;
	}

	public ResponseModel addSalesInfo(AddRequestTarget data) {
		String validationMessage = "";
		ResponseModel model = new ResponseModel();
		List<SalesInfo> listSalesInfo = new ArrayList<SalesInfo>();
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");
		BooleanExpression expression = QSalesInfo.salesInfo.id.isNotNull();
		expression = expression.and(QSalesInfo.salesInfo.tahun.eq(data.getTahun()));
		expression = expression.and(QSalesInfo.salesInfo.bup.eq(data.getBup()));
		Iterable<SalesInfo> oldData = salesInfoRepository.findAll(expression);
		if (data.getBup() == "" && data.getTahun() == "") {
			validationMessage = "Kolom BUP dan Tahun mandatory";
		} else if (oldData.spliterator().getExactSizeIfKnown() != 0) {
			validationMessage = String.format("Data sales target dan existing %s tahun %s sudah tersedia",
					data.getBup(), data.getTahun());
		} else {
			months.forEach(month -> {
				SalesInfo salesInfo = new SalesInfo();
				salesInfo.setBulan(month);
				salesInfo.setBup(data.getBup());
				salesInfo.setTahun(data.getTahun());
				Optional<MonthClass> dataEx = data.getExisting().stream()
						.filter(dataE -> month.equalsIgnoreCase(dataE.getBulan())).findFirst();
				if (dataEx.isPresent()) {
					salesInfo.setExisting(BigDecimal.valueOf(dataEx.get().getValue()));
				}

				Optional<MonthClass> dataTg = data.getTarget().stream()
						.filter(dataTg1 -> month.equalsIgnoreCase(dataTg1.getBulan())).findFirst();
				if (dataTg.isPresent()) {
					salesInfo.setTarget(BigDecimal.valueOf(dataTg.get().getValue()));
				}
				listSalesInfo.add(salesInfo);
			});

		}

		if (validationMessage != "") {
			model.setMessage(validationMessage.replaceFirst(",", ""));
			model.setSuccess("false");
			model.setStatusCode(HttpStatus.BAD_REQUEST.value());
		} else {
			model.setStatusCode(HttpStatus.CREATED.value());
			List<SalesInfo> save = salesInfoRepository.saveAllAndFlush(listSalesInfo);
			model.setData(save);
		}
		return model;
	}

	public ResponseModel deleteSalesInfo(Integer tahun, String bup) {
		ResponseModel model = new ResponseModel();
		BooleanExpression expression = QSalesInfo.salesInfo.id.isNotNull();
		expression = expression.and(QSalesInfo.salesInfo.tahun.eq(String.valueOf(tahun)));
		expression = expression.and(QSalesInfo.salesInfo.bup.eq(bup));
		Iterable<SalesInfo> detailData = salesInfoRepository.findAll(expression);
		if (detailData.spliterator().getExactSizeIfKnown() != 0) {
			salesInfoRepository.deleteAll(detailData);
		} else {
			model.setMessage("Data tidak di temukan");
		}
		return model;
	}

	public ResponseModel deleteSalesInfoAll() {
		ResponseModel model = new ResponseModel();
		salesInfoRepository.deleteAll();
		return model;
	}

	public ResponseModel detailSalesInfo(Integer tahun, String bup) {
		ResponseModel model = new ResponseModel();
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");

		BooleanExpression expression = QSalesInfo.salesInfo.id.isNotNull();
		expression = expression.and(QSalesInfo.salesInfo.tahun.eq(String.valueOf(tahun)));
		expression = expression.and(QSalesInfo.salesInfo.bup.eq(bup));
		Iterable<SalesInfo> detailData = salesInfoRepository.findAll(expression);
		DetailResponse detail = new DetailResponse();
		MonthName exist = new MonthName();
		MonthName target = new MonthName();
		if (detailData.spliterator().getExactSizeIfKnown() != 0) {
			months.forEach(month -> {
				Optional<SalesInfo> dataSalesInfo = StreamSupport.stream(detailData.spliterator(), false)
						.filter(sales -> sales.getBulan().equalsIgnoreCase(month)).findFirst();
				detail.setBup(dataSalesInfo.get().getBup());
				detail.setTahun(dataSalesInfo.get().getTahun());
				if (month.equalsIgnoreCase("January")) {
					exist.setJanuary(dataSalesInfo.get().getExisting());
					target.setJanuary(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("February")) {
					exist.setFebruary(dataSalesInfo.get().getExisting());
					target.setFebruary(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("March")) {
					exist.setMarch(dataSalesInfo.get().getExisting());
					target.setMarch(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("April")) {
					exist.setApril(dataSalesInfo.get().getExisting());
					target.setApril(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("May")) {
					exist.setMay(dataSalesInfo.get().getExisting());
					target.setMay(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("June")) {
					exist.setJune(dataSalesInfo.get().getExisting());
					target.setJune(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("July")) {
					exist.setJuly(dataSalesInfo.get().getExisting());
					target.setJuly(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("August")) {
					exist.setAugust(dataSalesInfo.get().getExisting());
					target.setAugust(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("September")) {
					exist.setSeptember(dataSalesInfo.get().getExisting());
					target.setSeptember(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("October")) {
					exist.setOctober(dataSalesInfo.get().getExisting());
					target.setOctober(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("November")) {
					exist.setNovember(dataSalesInfo.get().getExisting());
					target.setNovember(dataSalesInfo.get().getTarget());
				} else if (month.equalsIgnoreCase("December")) {
					exist.setDecember(dataSalesInfo.get().getExisting());
					target.setDecember(dataSalesInfo.get().getTarget());
				}
			});
			detail.setExisting(exist);
			detail.setTarget(target);
			model.setData(detail);
		} else {
			model.setMessage("Data tidak di temukan");
		}
		return model;
	}

	public ResponseModel editSalesInfo(AddRequestTarget data) {
		String validationMessage = "";
		ResponseModel model = new ResponseModel();
		List<SalesInfo> listSalesInfo = new ArrayList<SalesInfo>();
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");

		months.forEach(month -> {
			BooleanExpression expression = QSalesInfo.salesInfo.id.isNotNull();
			expression = expression.and(QSalesInfo.salesInfo.tahun.eq(data.getTahun()));
			expression = expression.and(QSalesInfo.salesInfo.bup.eq(data.getBup()));
			expression = expression.and(QSalesInfo.salesInfo.bulan.equalsIgnoreCase(month));

			Optional<SalesInfo> salesInfoO = salesInfoRepository.findOne(expression);
			if (salesInfoO.isPresent()) {
				SalesInfo salesInfo = salesInfoO.get();

//			salesInfo.setBulan(month);
//			salesInfo.setBup(data.getBup());
//			salesInfo.setTahun(data.getTahun());
				Optional<MonthClass> dataEx = data.getExisting().stream()
						.filter(dataE -> month.equalsIgnoreCase(dataE.getBulan())).findFirst();
				if (dataEx.isPresent()) {
					salesInfo.setExisting(BigDecimal.valueOf(dataEx.get().getValue()));
				}

				Optional<MonthClass> dataTg = data.getTarget().stream()
						.filter(dataTg1 -> month.equalsIgnoreCase(dataTg1.getBulan())).findFirst();
				if (dataTg.isPresent()) {
					salesInfo.setTarget(BigDecimal.valueOf(dataTg.get().getValue()));
				}
				listSalesInfo.add(salesInfo);
			}
		});

		if (validationMessage != "") {
			model.setMessage(validationMessage.replaceFirst(",", ""));
			model.setSuccess("false");
			model.setStatusCode(HttpStatus.BAD_REQUEST.value());
		} else {
			model.setData(listSalesInfo);
			salesInfoRepository.saveAllAndFlush(listSalesInfo);
		}

		return model;
	}
}
