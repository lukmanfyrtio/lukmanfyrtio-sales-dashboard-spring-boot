package com.id.sales.service.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.id.sales.service.dto.AddRequestTarget;
import com.id.sales.service.dto.DetailResponse;
import com.id.sales.service.dto.MonthClass;
import com.id.sales.service.dto.MonthName;
import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.CompanyTarget;
import com.id.sales.service.model.Department;
import com.id.sales.service.model.QCompanyTarget;
import com.id.sales.service.repository.CompanyTargetRepository;
import com.id.sales.service.repository.DepartmentRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class CompanyTargetService {

	@Autowired
	private CompanyTargetRepository companyTargetRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	public List<CompanyTarget> getAllCompanyTargets() {
		return companyTargetRepository.findAll();
	}

	public Optional<CompanyTarget> getCompanyTargetById(UUID companyTargetId) {
		return companyTargetRepository.findById(companyTargetId);
	}

	public CompanyTarget createCompanyTarget(CompanyTarget companyTarget) {
		return companyTargetRepository.save(companyTarget);
	}

	public CompanyTarget updateCompanyTarget(UUID companyTargetId, CompanyTarget updatedCompanyTarget) {
		if (companyTargetRepository.existsById(companyTargetId)) {
			updatedCompanyTarget.setId(companyTargetId);
			return companyTargetRepository.save(updatedCompanyTarget);
		}
		return null; // Handle not found case
	}

	public void deleteCompanyTarget(UUID companyTargetId) {
		companyTargetRepository.deleteById(companyTargetId);
	}

	public Page<CompanyTarget> filterCompanyTarget(UUID departementId, String search, Pageable pageable) {
		BooleanExpression predicate = QCompanyTarget.companyTarget.id.isNotNull();
		if (departementId != null) {
			predicate = predicate.and(QCompanyTarget.companyTarget.department.id.eq(departementId));
		}

		if (search != null) {
			predicate = predicate.and(QCompanyTarget.companyTarget.department.name.toUpperCase()
					.like("%" + search.toUpperCase() + "%")
					.or(QCompanyTarget.companyTarget.year.toUpperCase().like("%" + search.toUpperCase() + "%")));
		}

		Iterable<CompanyTarget> listI = companyTargetRepository.findAll(predicate);
		List<CompanyTarget> list = StreamSupport.stream(listI.spliterator(), false).collect(Collectors.toList());

		List<CompanyTarget> listDistict = list.stream()
				.filter(distinctByKey(p -> p.getYear() + p.getDepartment().getId())).collect(Collectors.toList());

		listDistict.forEach(dataD -> {
			String yearDepartement = dataD.getYear() + dataD.getDepartment().getId();
			Double existing = list.stream()
					.filter(data -> yearDepartement.equalsIgnoreCase(data.getYear() + data.getDepartment().getId()))
					.mapToDouble(x -> Double.valueOf(x.getExisting().doubleValue())).sum();
			Double target = list.stream()
					.filter(data -> yearDepartement.equalsIgnoreCase(data.getYear() + data.getDepartment().getId()))
					.mapToDouble(x -> Double.valueOf(x.getTarget().doubleValue())).sum();
			DecimalFormat formatter = new DecimalFormat("#0.00");
			dataD.setExisting(new BigDecimal(formatter.format(existing)));
			dataD.setTarget(new BigDecimal(formatter.format(target)));
		});

		int start = Math.min((int) pageable.getOffset(), listDistict.size());
		int end = Math.min((start + pageable.getPageSize()), listDistict.size());
		Page<CompanyTarget> pages = new PageImpl<CompanyTarget>(listDistict.subList(start, end), pageable,
				listDistict.size());
		return pages;
	}

	@Transactional
	public ResponseModel deleteTarget(Integer tahun, UUID departementId) {
		ResponseModel model = new ResponseModel();
		BooleanExpression expression = QCompanyTarget.companyTarget.id.isNotNull();
		expression = expression.and(QCompanyTarget.companyTarget.department.id.eq(departementId));
		Iterable<CompanyTarget> detailData = companyTargetRepository.findAll(expression);
		if (detailData.spliterator().getExactSizeIfKnown() != 0) {
			companyTargetRepository.deleteAll(detailData);
		} else {
			model.setMessage("Data tidak di temukan");
		}
		return model;
	}

	public ResponseModel addUnitTarget(AddRequestTarget data) {
		Department department = departmentRepository.findById(data.getDepartmentId()).orElse(new Department());
		String validationMessage = "";
		ResponseModel model = new ResponseModel();
		List<CompanyTarget> listSalesInfo = new ArrayList<CompanyTarget>();
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");
		BooleanExpression expression = QCompanyTarget.companyTarget.id.isNotNull();
		expression = expression.and(QCompanyTarget.companyTarget.year.eq(data.getYear()));
		expression = expression.and(QCompanyTarget.companyTarget.department.id.eq(data.getDepartmentId()));
		Iterable<CompanyTarget> oldData = companyTargetRepository.findAll(expression);
		if (data.getDepartmentId() == null && data.getYear() == "") {
			validationMessage = "departmentId dan year is mandatory";
		} else if (oldData.spliterator().getExactSizeIfKnown() != 0) {
			validationMessage = String.format("Company  target dan existing %s tahun %s is exist",
					department.getName(), data.getYear());
		} else {
			months.forEach(month -> {
				CompanyTarget salesInfo = new CompanyTarget();
				salesInfo.setMonth(month);
				salesInfo.setDepartment(department);
				salesInfo.setYear(data.getYear());
				Optional<MonthClass> dataEx = data.getExisting().stream()
						.filter(dataE -> month.equalsIgnoreCase(dataE.getMonth())).findFirst();
				if (dataEx.isPresent()) {
					salesInfo.setExisting(BigDecimal.valueOf(dataEx.get().getValue()));
				}

				Optional<MonthClass> dataTg = data.getTarget().stream()
						.filter(dataTg1 -> month.equalsIgnoreCase(dataTg1.getMonth())).findFirst();
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
			List<CompanyTarget> save = companyTargetRepository.saveAllAndFlush(listSalesInfo);
			model.setData(save);
		}
		return model;
	}

	public ResponseModel detailCompanyTarget(Integer year, UUID departmentId) {
		ResponseModel model = new ResponseModel();
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");

		BooleanExpression expression = QCompanyTarget.companyTarget.id.isNotNull();
		expression = expression.and(QCompanyTarget.companyTarget.year.eq(String.valueOf(year)));
		expression = expression.and(QCompanyTarget.companyTarget.department.id.eq(departmentId));
		Iterable<CompanyTarget> detailData = companyTargetRepository.findAll(expression);
		DetailResponse detail = new DetailResponse();
		MonthName exist = new MonthName();
		MonthName target = new MonthName();
		if (detailData.spliterator().getExactSizeIfKnown() != 0) {
			months.forEach(month -> {
				Optional<CompanyTarget> dataCompanyTarget = StreamSupport.stream(detailData.spliterator(), false)
						.filter(sales -> sales.getMonth().equalsIgnoreCase(month)).findFirst();
				detail.setDepartmentId(dataCompanyTarget.get().getDepartment().getId());
				detail.setYear(dataCompanyTarget.get().getYear());
				if (month.equalsIgnoreCase("January")) {
					exist.setJanuary(dataCompanyTarget.get().getExisting());
					target.setJanuary(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("February")) {
					exist.setFebruary(dataCompanyTarget.get().getExisting());
					target.setFebruary(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("March")) {
					exist.setMarch(dataCompanyTarget.get().getExisting());
					target.setMarch(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("April")) {
					exist.setApril(dataCompanyTarget.get().getExisting());
					target.setApril(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("May")) {
					exist.setMay(dataCompanyTarget.get().getExisting());
					target.setMay(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("June")) {
					exist.setJune(dataCompanyTarget.get().getExisting());
					target.setJune(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("July")) {
					exist.setJuly(dataCompanyTarget.get().getExisting());
					target.setJuly(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("August")) {
					exist.setAugust(dataCompanyTarget.get().getExisting());
					target.setAugust(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("September")) {
					exist.setSeptember(dataCompanyTarget.get().getExisting());
					target.setSeptember(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("October")) {
					exist.setOctober(dataCompanyTarget.get().getExisting());
					target.setOctober(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("November")) {
					exist.setNovember(dataCompanyTarget.get().getExisting());
					target.setNovember(dataCompanyTarget.get().getTarget());
				} else if (month.equalsIgnoreCase("December")) {
					exist.setDecember(dataCompanyTarget.get().getExisting());
					target.setDecember(dataCompanyTarget.get().getTarget());
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

	public ResponseModel editCompanyTarget(AddRequestTarget data) {
		String validationMessage = "";
		ResponseModel model = new ResponseModel();
		List<CompanyTarget> listCompanyTarget = new ArrayList<CompanyTarget>();
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");

		months.forEach(month -> {
			BooleanExpression expression = QCompanyTarget.companyTarget.id.isNotNull();
			expression = expression.and(QCompanyTarget.companyTarget.year.eq(data.getYear()));
			expression = expression.and(QCompanyTarget.companyTarget.department.id.eq(data.getDepartmentId()));
			expression = expression.and(QCompanyTarget.companyTarget.month.equalsIgnoreCase(month));

			Optional<CompanyTarget> companyTargetO = companyTargetRepository.findOne(expression);
			if (companyTargetO.isPresent()) {
				CompanyTarget companyTarget = companyTargetO.get();

//			companyTarget.setBulan(month);
//			companyTarget.setBup(data.getBup());
//			companyTarget.setTahun(data.getYear());
				Optional<MonthClass> dataEx = data.getExisting().stream()
						.filter(dataE -> month.equalsIgnoreCase(dataE.getMonth())).findFirst();
				if (dataEx.isPresent()) {
					companyTarget.setExisting(BigDecimal.valueOf(dataEx.get().getValue()));
				}

				Optional<MonthClass> dataTg = data.getTarget().stream()
						.filter(dataTg1 -> month.equalsIgnoreCase(dataTg1.getMonth())).findFirst();
				if (dataTg.isPresent()) {
					companyTarget.setTarget(BigDecimal.valueOf(dataTg.get().getValue()));
				}
				listCompanyTarget.add(companyTarget);
			}
		});

		if (validationMessage != "") {
			model.setMessage(validationMessage.replaceFirst(",", ""));
			model.setSuccess("false");
			model.setStatusCode(HttpStatus.BAD_REQUEST.value());
		} else {
			model.setData(listCompanyTarget);
			companyTargetRepository.saveAllAndFlush(listCompanyTarget);
		}

		return model;
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
