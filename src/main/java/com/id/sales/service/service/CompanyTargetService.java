package com.id.sales.service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.id.sales.service.model.CompanyTarget;
import com.id.sales.service.repository.CompanyTargetRepository;

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
}
