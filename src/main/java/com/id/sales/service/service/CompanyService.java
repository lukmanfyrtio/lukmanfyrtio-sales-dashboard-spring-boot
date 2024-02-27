package com.id.sales.service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.id.sales.service.model.Company;
import com.id.sales.service.repository.CompanyRepository;

@Service
public class CompanyService {

	@Autowired
	private CompanyRepository companyRepository;

	public List<Company> getAllCompanies() {
		return companyRepository.findAll();
	}

	public Optional<Company> getCompanyById(UUID companyId) {
		return companyRepository.findById(companyId);
	}

	public Company createCompany(Company company) {
		return companyRepository.save(company);
	}

	public Company updateCompany(UUID companyId, Company updatedCompany) {
		if (companyRepository.existsById(companyId)) {
			updatedCompany.setId(companyId);
			return companyRepository.save(updatedCompany);
		}
		return null; // Handle not found case
	}

	public void deleteCompany(UUID companyId) {
		companyRepository.deleteById(companyId);
	}
}
