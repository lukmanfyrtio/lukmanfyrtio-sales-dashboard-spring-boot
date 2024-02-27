package com.id.sales.service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.id.sales.service.model.SalesRevenue;
import com.id.sales.service.repository.SalesRevenueRepository;

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
}