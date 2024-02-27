package com.id.sales.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.id.sales.service.model.Company;
import com.id.sales.service.model.CompanyTarget;
import com.id.sales.service.model.Department;
import com.id.sales.service.model.Product;
import com.id.sales.service.model.SalesLeads;
import com.id.sales.service.model.SalesRevenue;
import com.id.sales.service.repository.CompanyRepository;
import com.id.sales.service.repository.CompanyTargetRepository;
import com.id.sales.service.repository.DepartmentRepository;
import com.id.sales.service.repository.ProductRepository;
import com.id.sales.service.repository.SalesRevenueRepository;
import com.id.sales.service.service.SalesLeadsService;

@SpringBootApplication
public class SalesPerformanceServiceApplication {
	
	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private CompanyTargetRepository companyTargetRepository;
	
	@Autowired
	private SalesLeadsService salesLeadsService;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private SalesRevenueRepository salesRevenueRepository;

	public static void main(String[] args) {
		SpringApplication.run(SalesPerformanceServiceApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init() {
		return args -> {
			
			List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
					"September", "October", "November", "December");
			
			
			Company company=new Company();
			company.setName("PT Swamedia Informatika");
			company=companyRepository.save(company);
			
			List<Department>departments=new ArrayList<>();
			
			Department department=new Department();
			department.setName("SES");
			department.setCompany(company);
			department.setDisplay(true);
			department=departmentRepository.save(department);
			
			Department department2=new Department();
			department2.setName("BIL");
			department2.setCompany(company);
			department2.setDisplay(true);
			department2=departmentRepository.save(department2);
			
			Department department3=new Department();
			department3.setName("DES");
			department3.setCompany(company);
			department3.setDisplay(true);
			department3=departmentRepository.save(department3);
			
			departments.add(department);
			departments.add(department2);
			departments.add(department3);
			
			for (Department data : departments) {
				for (String string : months) {
					Random random = new Random();
					CompanyTarget companyTarget=new CompanyTarget();
					companyTarget.setDepartment(data);
					companyTarget.setExisting(new BigDecimal(1));
					companyTarget.setTarget(new BigDecimal(random.nextInt(6)));
					companyTarget.setMonth(string);
					companyTarget.setYear("2024");
					companyTargetRepository.save(companyTarget);
				}
			}
			
			Product product=new Product();
			
			product.setName("APICENTRUM - API MANAGEMENT");
			
			product.setDepartment(department);
			
			Product product1=new Product();
			
			product1.setName("E-Bill");
			
			product1.setDepartment(department2);
			
			Product product3=new Product();
			
			product3.setName("Bonefire");
			
			product3.setDepartment(department3);
			
			product=productRepository.save(product);
			product1=productRepository.save(product1);
			product3=productRepository.save(product3);
			
			
            SalesLeads salesLeads = new SalesLeads();
            salesLeads.setMonth("January");
            salesLeads.setSalesName("John Doe");
            salesLeads.setPotentialCustomer("ABC Corp");
            salesLeads.setAddress("123 Main St");
            salesLeads.setPostalCode("12345");
            salesLeads.setPhoneNumber("555-1234");
            salesLeads.setEmail("john.doe@example.com");
            salesLeads.setProduct(product);
            salesLeads.setProjectedValue("1000000000");
            salesLeads.setLeadsCategory("Medium");
            salesLeads.setCurrentStage("Opportunities");
            salesLeads.setLeadsStatus("Open");
            salesLeads.setNotes("Sample notes");
            
            
            SalesLeads salesLeads2 = new SalesLeads();
            salesLeads2.setMonth("February");
            salesLeads2.setSalesName("John Snow");
            salesLeads2.setPotentialCustomer("BCA Corp");
            salesLeads2.setAddress("321 Main St");
            salesLeads2.setPostalCode("098721");
            salesLeads2.setPhoneNumber("111-1234");
            salesLeads2.setEmail("john.snow@example.com");
            salesLeads2.setProduct(product1);
            salesLeads2.setProjectedValue("300000000");
            salesLeads2.setLeadsCategory("High");
            salesLeads2.setCurrentStage("Opportunities");
            salesLeads2.setLeadsStatus("Open");
            salesLeads2.setNotes("Sample notes");
            
            salesLeads2=salesLeadsService.createSalesLeads(salesLeads2);
            salesLeads=salesLeadsService.createSalesLeads(salesLeads);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            SalesRevenue salesRevenue1 = new SalesRevenue();
            salesRevenue1.setInvoiceNumber("INV-001");
            salesRevenue1.setInvoiceDate("2022/01/15");
            salesRevenue1.setPrincipalReceipt("2000000000");
            // Set other properties...
            salesRevenue1.setDepartment(department2);
            salesRevenue1.setSalesLeads(salesLeads);
            salesRevenue1.setPrincipalReceiptEntryDate(dateFormat.parse("2024/01/20"));
            salesRevenueRepository.save(salesRevenue1);

            SalesRevenue salesRevenue2 = new SalesRevenue();
            salesRevenue2.setInvoiceNumber("INV-002");
            salesRevenue2.setInvoiceDate("2022/02/20");
            salesRevenue2.setDepartment(department2);
            salesRevenue2.setSalesLeads(salesLeads2);
            salesRevenue2.setPrincipalReceiptEntryDate(dateFormat.parse("2024/02/01"));
            salesRevenue2.setPrincipalReceipt("3000000000");
            salesRevenueRepository.save(salesRevenue2);

            SalesRevenue salesRevenue3 = new SalesRevenue();
            salesRevenue3.setInvoiceNumber("INV-003");
            salesRevenue3.setInvoiceDate("2024/03/25");
            salesRevenue3.setDepartment(department);
            salesRevenue3.setSalesLeads(salesLeads);
            salesRevenue3.setPrincipalReceipt("4000000000");
            salesRevenue3.setPrincipalReceiptEntryDate(dateFormat.parse("2024/02/29"));
            salesRevenueRepository.save(salesRevenue3);
            
		};
		
		
		
	}
}
