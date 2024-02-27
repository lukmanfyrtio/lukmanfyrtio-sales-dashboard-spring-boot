package com.id.sales.service.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import org.springframework.stereotype.Service;

import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.model.CompanyTarget;
import com.id.sales.service.model.QCompanyTarget;
import com.id.sales.service.repository.CompanyTargetRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class CompanyTargetService {

	@Autowired
	private CompanyTargetRepository companyTargetRepository;

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
			predicate = predicate
					.and(QCompanyTarget.companyTarget.department.name.toUpperCase().like("%" + search.toUpperCase() + "%").or(
							QCompanyTarget.companyTarget.year.toUpperCase().like("%" + search.toUpperCase() + "%")));
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

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
