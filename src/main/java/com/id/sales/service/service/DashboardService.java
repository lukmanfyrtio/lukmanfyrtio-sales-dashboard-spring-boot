package com.id.sales.service.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.id.sales.service.dto.ResponseModel;
import com.id.sales.service.dto.SalesFunnel;
import com.id.sales.service.dto.TargetBreakdown;
import com.id.sales.service.model.CompanyTarget;
import com.id.sales.service.model.Department;
import com.id.sales.service.model.QCompanyTarget;
import com.id.sales.service.model.SalesLeads;
import com.id.sales.service.repository.CompanyTargetRepository;
import com.id.sales.service.repository.DepartmentRepository;
import com.id.sales.service.repository.SalesLeadsRepository;
import com.id.sales.service.repository.SalesRevenueRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class DashboardService {
	@Autowired
	private SalesLeadsRepository salesLeadsRepository;

	@Autowired
	private CompanyTargetRepository companyTargetRepository;

	@Autowired
	private SalesRevenueRepository salesRevenueRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	public ResponseModel getTargetBreakdown(String year, UUID departementUUID) {
		ResponseModel model = new ResponseModel();

		model.setData(getTarget(year, departementUUID));
		return model;
	}
	
	public ResponseModel getDetailDashboard(UUID departementUUID, String tahun) {
		ResponseModel model = new ResponseModel();

		List<SalesLeads> custInfo = salesLeadsRepository.getListByDepartmentIdAndYear(departementUUID, Integer.valueOf(tahun));

		List<Long> avgSalesC = new ArrayList<Long>();
		SimpleDateFormat myFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		custInfo.forEach(cust -> {
			if (cust.getCurrentStage().equalsIgnoreCase("Deals")) {
				long diffDay = gitDiffDay(cust.getOpportunitiesOpen(),
						cust.getDealsOpen() != null ? cust.getDealsOpen() :new Date());
				avgSalesC.add(diffDay);
			}
		});
		double avgSales = calculateAverage(avgSalesC);
		System.out.println("avgSalesCycle= " + calculateAverage(avgSalesC));

		long droppedCount = salesLeadsRepository
				.getListByStageBUPAndYear(departementUUID, Integer.valueOf(tahun), "Dropped", "Close").size();
		long dealsCount = salesLeadsRepository
				.getListByStageBUPAndYear(departementUUID, Integer.valueOf(tahun), "Deals", "Close").size();
		long negotiationCount = salesLeadsRepository
				.getListByStageBUPAndYear(departementUUID, Integer.valueOf(tahun), "Negotiation", "Open").size();
		long proposalCount = salesLeadsRepository
				.getListByStageBUPAndYear(departementUUID, Integer.valueOf(tahun), "Proposal", "Open").size();
		long opportunitiesCount = salesLeadsRepository
				.getListByStageBUPAndYear(departementUUID, Integer.valueOf(tahun), "Opportunities", "Open").size();
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		SalesFunnel salesFunnel = new SalesFunnel();
		salesFunnel.setDeals(String.valueOf(dealsCount));
		salesFunnel.setDropped(String.valueOf(droppedCount));
		salesFunnel.setNegotiation(String.valueOf(negotiationCount));
		salesFunnel.setProposal(String.valueOf(proposalCount));
		salesFunnel.setOpportunities(String.valueOf(opportunitiesCount));
		salesFunnel.setAvgSalesCycle(String.valueOf(Math.round(avgSales)));
		Date lstUpd = salesLeadsRepository.getLastUpdate(departementUUID, Integer.valueOf(tahun));
		Date createdDate = salesLeadsRepository.getLastCreated(departementUUID, Integer.valueOf(tahun));
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

	public ResponseModel getExistGAP(String tahun, UUID departementUUID) {
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");
		// sales funnel
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		Iterable<SalesLeads> prospekList = salesLeadsRepository.getListByDepartmentIdAndYear(departementUUID,
				Integer.valueOf(tahun));
		ResponseModel model = new ResponseModel();

		List<String> existing = new ArrayList<String>();
		List<String> gap = new ArrayList<String>();
		List<String> prospekS = new ArrayList<String>();

		BooleanExpression expression = QCompanyTarget.companyTarget.id.isNotNull();
		expression = expression.and(QCompanyTarget.companyTarget.year.eq(tahun));
		if(departementUUID != null) {
			expression = expression.and(QCompanyTarget.companyTarget.department.id.eq(departementUUID));
		}
		List<CompanyTarget> salesInfo = StreamSupport
				.stream(companyTargetRepository.findAll(expression).spliterator(), true).collect(Collectors.toList());

		double lastProspek = 0;
		for (String month : months) {
			CompanyTarget salesData = new CompanyTarget();
			if (salesInfo.isEmpty()) {
				salesData.setExisting(BigDecimal.ZERO);
				salesData.setTarget(BigDecimal.ZERO);
			} else {
				salesData = salesInfo.stream().filter(salesInfoData -> salesInfoData.getYear().equalsIgnoreCase(month))
						.findFirst().orElse(null);
			}
			
			
			if (salesData != null) {
				Double existingPermonth = salesData.getExisting().doubleValue();
				Double targetPerMonth = salesData.getTarget().doubleValue();
				Double prospekPerMonth = StreamSupport.stream(prospekList.spliterator(), false)
						.filter(c -> month.equalsIgnoreCase(c.getMonth()))
						.mapToDouble(x -> Double
								.valueOf(x.getProjectedValue().isEmpty() || x.getProjectedValue().isBlank() ? "0"
										: x.getProjectedValue()))
						.sum() / 1000000000.0;
				existing.add(df.format(existingPermonth));

//				prospekS.add(df.format(prospekPerMonth));

				System.out.println("prospekPM=" + prospekPerMonth);
				if (prospekPerMonth != 0.0) {
					lastProspek += prospekPerMonth / (12 - (months.indexOf(month) + 1));
					System.out.println("lastP=" + lastProspek);
				}
				prospekS.add(df.format(lastProspek));
				prospekS.add(df.format(Double.valueOf(targetPerMonth - existingPermonth - lastProspek)));
			}else {
				existing.add("0");
				prospekS.add("0");
				gap.add("0");
			}

		}
		;
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("existing", existing);
		result.put("gap", gap);
		result.put("prospek", prospekS);
		model.setData(result);
		return model;
	}

	public ResponseModel getTargetVsActual(String tahun, UUID departementUUID) {
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");
		ResponseModel model = new ResponseModel();

		List<String> actualList = new ArrayList<String>();
		List<String> targetList = new ArrayList<String>();

		BooleanExpression expression = QCompanyTarget.companyTarget.id.isNotNull();
		expression = expression.and(QCompanyTarget.companyTarget.year.eq(tahun));
		if (departementUUID != null) {
			expression = expression.and(QCompanyTarget.companyTarget.department.id.eq(departementUUID));
		}
		List<CompanyTarget> salesInfo = StreamSupport
				.stream(companyTargetRepository.findAll(expression).spliterator(), true).collect(Collectors.toList());

		months.forEach(month -> {
			try {
				CompanyTarget dataS = StreamSupport.stream(salesInfo.spliterator(), false)
						.filter(sales -> sales.getYear().equalsIgnoreCase(month)).findFirst().get();
				targetList.add(dataS.getTarget().toString());
				System.out.println(dataS.getTarget());
			} catch (Exception e) {
				targetList.add("0");
			}
			try {
				BigDecimal total = nvl(salesRevenueRepository.getSumPrincipalReceiptByMonthDepartmentIdYear(month,
						Integer.valueOf(tahun), departementUUID));
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

	public ResponseModel getTargetBreakdownALL(String tahun) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		ResponseModel model = new ResponseModel();
		TargetBreakdown result = new TargetBreakdown("0", "0", "0", "0", "0", "0", "0");
		List<Department> departments = departmentRepository.findTop3ByIsDisplayTrue();

		for (Department department : departments) {
			TargetBreakdown target = getTarget(tahun, department.getId());

			// Accumulate the values for each department
			result.setSalesFunnel(
					df.format(Double.valueOf(result.getSalesFunnel()) + Double.valueOf(target.getSalesFunnel())));

			result.setCashInTarget(
					df.format(Double.valueOf(result.getCashInTarget()) + Double.valueOf(target.getCashInTarget())));

			result.setExistingCustomer(df.format(
					Double.valueOf(result.getExistingCustomer()) + Double.valueOf(target.getExistingCustomer())));

			result.setGAP(df.format(Double.valueOf(result.getGAP()) + Double.valueOf(target.getGAP())));

			result.setMtdCashIn(
					df.format(Double.valueOf(result.getMtdCashIn()) + Double.valueOf(target.getMtdCashIn())));

			result.setMtdTarget(
					df.format(Double.valueOf(result.getMtdTarget()) + Double.valueOf(target.getMtdTarget())));
		}

		// Calculate achievement based on accumulated values
		double mtdTarget = Double.valueOf(result.getMtdTarget());
		double mtdCashIn = Double.valueOf(result.getMtdCashIn());

		double achievement = (Double.isFinite(mtdTarget) && mtdTarget != 0) ? mtdCashIn / mtdTarget * 100 : 0.0;

		result.setAchievement(df.format(achievement));
		model.setData(result);
		return model;
	}

	private TargetBreakdown getTarget(String year, UUID departementUUID) {
		TargetBreakdown tg = new TargetBreakdown();
		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");

		BooleanExpression expression = QCompanyTarget.companyTarget.id.isNotNull();
		expression = expression.and(QCompanyTarget.companyTarget.year.eq(String.valueOf(year)));
		if (departementUUID != null) {
			expression = expression.and(QCompanyTarget.companyTarget.department.id.eq(departementUUID));
		}
		List<CompanyTarget> salesInfo = StreamSupport
				.stream(companyTargetRepository.findAll(expression).spliterator(), true).collect(Collectors.toList());

		String departementTahun = departementUUID != null ? year + departementUUID : year;

		// existingCustomer
		Double existing = salesInfo.stream()
				.filter(data -> departementTahun.equalsIgnoreCase(
						departementUUID != null ? data.getYear() + data.getDepartment().getId() : data.getYear()))
				.mapToDouble(x -> Double.valueOf(x.getExisting().doubleValue())).sum();

		// sales funnel
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		Iterable<SalesLeads> prospekList = salesLeadsRepository.getListByDepartmentIdAndYear(departementUUID,
				Integer.valueOf(year));
		Double prospekCount = StreamSupport.stream(prospekList.spliterator(), false).mapToDouble(x -> Double.valueOf(
				x.getProjectedValue().isEmpty() || x.getProjectedValue().isBlank() ? "0" : x.getProjectedValue())).sum()
				/ 1000000000.0;

		SimpleDateFormat date = new SimpleDateFormat("MMMM");
		String bulanNow = date.format(new Date());

		double mtdTarget = 0;
		double mtdcashIn = 0;
		double gap = 0;
		for (String month : months) {
			CompanyTarget salesData = new CompanyTarget();
			if (salesInfo.isEmpty()) {
				salesData.setExisting(BigDecimal.ZERO);
				salesData.setTarget(BigDecimal.ZERO);
			} else {
				salesData = salesInfo.stream().filter(salesInfoData -> salesInfoData.getMonth().equalsIgnoreCase(month))
						.findFirst().get();
			}

			Double cashInPerMonth = StreamSupport.stream(prospekList.spliterator(), false)
					.filter(c -> month.equalsIgnoreCase(c.getMonth()))
					.mapToDouble(
							x -> Double.valueOf(x.getProjectedValue().isEmpty() || x.getProjectedValue().isBlank() ? "0"
									: x.getProjectedValue()))
					.sum() / 1000000000.0;

			BigDecimal pPokok = salesRevenueRepository.getSumPrincipalReceiptByMonthDepartmentIdYear(month,
					Integer.valueOf(year), departementUUID);
			System.out.println(month + cashInPerMonth);
			mtdTarget = Double.sum(mtdTarget, salesData.getTarget().doubleValue());
			String toBn = toBn(nvl(pPokok));
			mtdcashIn = Double.sum(mtdcashIn, Double.valueOf(toBn));
			System.out.println("bulan = " + month + " cash in =" + pPokok);
			if (month.equalsIgnoreCase(bulanNow)) {
				break;
			}
		}

		for (String month : months) {
			CompanyTarget salesData = new CompanyTarget();
			if (salesInfo.isEmpty()) {
				salesData.setExisting(BigDecimal.ZERO);
				salesData.setTarget(BigDecimal.ZERO);
			} else {
				salesData = salesInfo.stream().filter(salesInfoData -> salesInfoData.getMonth().equalsIgnoreCase(month))
						.findFirst().get();
			}

			Double existingPermonth = salesData.getExisting().doubleValue();
			Double targetPerMonth = salesData.getTarget().doubleValue();
			Double prospekPerMonth = StreamSupport.stream(prospekList.spliterator(), false)
					.filter(c -> month.equalsIgnoreCase(c.getMonth()))
					.mapToDouble(
							x -> Double.valueOf(x.getProjectedValue().isEmpty() || x.getProjectedValue().isBlank() ? "0"
									: x.getProjectedValue()))
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

	
	public ResponseModel getDataSales(UUID departementUUID, String tahun, Integer page, Integer size) {
		ResponseModel model = new ResponseModel();

		List<SalesLeads> custInfo = salesLeadsRepository.getListByDepartmentIdAndYear(departementUUID, Integer.valueOf(tahun));

		List<SalesLeads> listDistictSales = StreamSupport.stream(custInfo.spliterator(), false)
				.filter(distinctByKey(p -> p.getSalesName())).collect(Collectors.toList());

		List<Map<String, Object>> salesResInfo = new ArrayList<Map<String, Object>>();
		listDistictSales.forEach(sales -> {
			List<Long> avgSalesC = new ArrayList<Long>();

			custInfo.forEach(cust -> {
				if (cust.getSalesName().equals(sales.getSalesName())) {
					if (!cust.getCurrentStage().equalsIgnoreCase("Dropped")) {
						long diffDay = gitDiffDay(cust.getOpportunitiesOpen(),
								cust.getDealsOpen() != null ? cust.getDealsOpen() : new Date());
						avgSalesC.add(diffDay);
					}
				}
			});

			long droppedCount = salesLeadsRepository.getListByStageBUPAndYearSalesName(departementUUID, Integer.valueOf(tahun),
					"Dropped", "Close", sales.getSalesName()).size();
			long dealsCount = salesLeadsRepository.getListByStageBUPAndYearSalesName(departementUUID, Integer.valueOf(tahun),
					"Deals", "Close", sales.getSalesName()).size();
			long negotiationCount = salesLeadsRepository.getListByStageBUPAndYearSalesName(departementUUID,
					Integer.valueOf(tahun), "Negotiation", "Open", sales.getSalesName()).size();
			long proposalCount = salesLeadsRepository.getListByStageBUPAndYearSalesName(departementUUID, Integer.valueOf(tahun),
					"Proposal", "Open", sales.getSalesName()).size();
			long opportunitiesCount = salesLeadsRepository.getListByStageBUPAndYearSalesName(departementUUID,
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
	
	public ResponseModel getAvgSales(UUID departementUUID, String tahun) {
		ResponseModel model = new ResponseModel();
		List<Long> op = new ArrayList<Long>();
		List<Long> pr = new ArrayList<Long>();
		List<Long> ng = new ArrayList<Long>();
		List<Long> dl = new ArrayList<Long>();
		List<SalesLeads> custInfo = salesLeadsRepository.getListByDepartmentIdAndYear(departementUUID, Integer.valueOf(tahun));
		custInfo.forEach(cust -> {

			if (cust.getCurrentStage().equalsIgnoreCase("Opportunities")) {
				long diffDay = gitDiffDay(cust.getOpportunitiesOpen(),
						cust.getProposalOpen() != null ? cust.getProposalOpen() : new Date());
				op.add(diffDay);

			} else if (cust.getCurrentStage().equalsIgnoreCase("Proposal")) {
				long diffDay = gitDiffDay(cust.getProposalOpen(),
						cust.getNegotiationOpen() != null ? cust.getNegotiationOpen() :new Date());
				pr.add(diffDay);

			} else if (cust.getCurrentStage().equalsIgnoreCase("Negotiation")) {
				long diffDay = gitDiffDay(cust.getNegotiationOpen(),
						cust.getDealsOpen() != null ? cust.getDealsOpen() : new Date());
				ng.add(diffDay);

			} else if (cust.getCurrentStage().equalsIgnoreCase("Deals")) {
				long diffDay = gitDiffDay(cust.getOpportunitiesOpen(),
						cust.getDealsOpen() != null ? cust.getDealsOpen() : new Date());
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
	
	public static BigDecimal nvl(final BigDecimal bigDecimal) {
		return bigDecimal == null ? BigDecimal.ZERO : bigDecimal;
	}

	public static String toBn(final BigDecimal bigDecimal) {
		BigDecimal billion = bigDecimal.divide(new BigDecimal("1000000000"));
		DecimalFormat decimalFormat = new DecimalFormat("0.0");
		return decimalFormat.format(billion);
	}
	
	private double calculateAverage(List<Long> marks) {
		return marks.stream().mapToDouble(d -> d).average().orElse(0.0);
	}

	private long gitDiffDay(Date date1, Date date2) {
	    try {
	        if (date2 == null) {
	            date2 = new Date(); // Default to current date if date2 is not provided
	        }
	        
	        long diff = date2.getTime() - date1.getTime();
	        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return 0;
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
