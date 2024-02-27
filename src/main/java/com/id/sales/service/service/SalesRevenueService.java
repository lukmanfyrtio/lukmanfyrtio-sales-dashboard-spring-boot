package com.id.sales.service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
}