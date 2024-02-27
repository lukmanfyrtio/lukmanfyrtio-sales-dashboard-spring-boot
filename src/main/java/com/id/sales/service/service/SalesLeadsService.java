package com.id.sales.service.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.id.sales.service.model.SalesLeads;
import com.id.sales.service.repository.SalesLeadsRepository;

@Service
public class SalesLeadsService {

	@Autowired
	private SalesLeadsRepository salesLeadsRepository;

	public List<SalesLeads> getAllSalesLeads() {
		try {
			return salesLeadsRepository.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}

	public Optional<SalesLeads> getSalesLeadsById(UUID salesLeadsId) {
		return salesLeadsRepository.findById(salesLeadsId);
	}

	public SalesLeads createSalesLeads(SalesLeads salesLeads) {
		updateDatesOnAdd(salesLeads);
		return salesLeadsRepository.save(salesLeads);
	}

	private void updateDatesOnAdd(SalesLeads data) {
		Date currentDate = new Date();

		if (data.getCurrentStage() != null) {
			switch (data.getCurrentStage()) {
			case "Opportunities":
				if (data.getOpportunitiesOpen() == null) {
					data.setOpportunitiesOpen(currentDate);
				}
				break;
			case "Proposal":
				if (data.getOpportunitiesClose() == null) {
					data.setOpportunitiesClose(currentDate);
				}
				data.setProposalOpen(currentDate);
				break;
			case "Negotiation":
				if (data.getProposalClose() == null) {
					data.setProposalClose(currentDate);
				}
				data.setNegotiationOpen(currentDate);
				break;
			case "Deals":
				if (data.getNegotiationClose() == null) {
					data.setNegotiationClose(currentDate);
				}
				data.setDealsOpen(currentDate);
				data.setDealsClose(currentDate);
				break;
			case "Dropped":
				if (data.getDroppedOpen() == null) {
					data.setDroppedOpen(currentDate);
				}
				if (data.getDroppedClose() == null) {
					data.setDroppedClose(currentDate);
				}
				break;
			default:
				break;
			}
		}
	}

	public SalesLeads updateSalesLeads(UUID salesLeadsId, SalesLeads updatedSalesLeads) {
		if (salesLeadsRepository.existsById(salesLeadsId)) {
			updatedSalesLeads.setId(salesLeadsId);
			updateDatesOnAdd(updatedSalesLeads);
			return salesLeadsRepository.save(updatedSalesLeads);
		}
		return null; // Handle not found case
	}

	public void deleteSalesLeads(UUID salesLeadsId) {
		salesLeadsRepository.deleteById(salesLeadsId);
	}

	public Page<SalesLeads> filterSalesLeads(String currentStage, String leadStatus, UUID departmentId, String search,
			Pageable pageable) {
		return salesLeadsRepository.filterSalesLeads(currentStage, leadStatus, departmentId, search, pageable);
	}

}
