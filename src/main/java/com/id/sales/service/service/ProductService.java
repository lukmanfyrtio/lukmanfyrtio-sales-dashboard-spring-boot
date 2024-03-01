package com.id.sales.service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.id.sales.service.model.Department;
import com.id.sales.service.model.Product;
import com.id.sales.service.model.QProduct;
import com.id.sales.service.repository.ProductRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	public Optional<Product> getProductById(UUID productId) {
		return productRepository.findById(productId);
	}

	public Product createProduct(Product product) {
		return productRepository.save(product);
	}

	public Product updateProduct(UUID productId, Product updatedProduct) {
		if (productRepository.existsById(productId)) {
			updatedProduct.setId(productId);
			return productRepository.save(updatedProduct);
		}
		return null; // Handle not found case
	}

	public void deleteProduct(UUID productId) {
		productRepository.deleteById(productId);
	}

	public List<Product> getProductByDepartmentId(UUID productUUID) {
		return productRepository.findByDepartment_Id(productUUID);
	}
	
	public Page<Product> filter(String search, Pageable pageable) {
		BooleanExpression predicate = QProduct.product.id.isNotNull();
		if (search != null) {
			predicate = predicate.and(QProduct.product.name.toLowerCase().like("%" + search.toLowerCase() + "%"))
					.or(QProduct.product.department.name.like("%" + search.toLowerCase() + "%"));
		}
		return productRepository.findAll(predicate,pageable);
	}
}
