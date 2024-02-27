package com.id.sales.service.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.id.sales.service.model.UserMapping;
import com.id.sales.service.service.UserMappingService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/usermappings")
@SecurityRequirement(name = "bearerAuth")
public class UserMappingController {

	@Autowired
	private UserMappingService userMappingService;

	@GetMapping
	public List<UserMapping> getAllUserMappings() {
		return userMappingService.getAllUserMappings();
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserMapping> getUserMappingById(@PathVariable UUID id) {
		Optional<UserMapping> userMapping = userMappingService.getUserMappingById(id);
		return userMapping.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<UserMapping> createUserMapping(@RequestBody UserMapping userMapping) {
		UserMapping createdUserMapping = userMappingService.createUserMapping(userMapping);
		return ResponseEntity.ok(createdUserMapping);
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserMapping> updateUserMapping(@PathVariable UUID id,
			@RequestBody UserMapping updatedUserMapping) {
		UserMapping userMapping = userMappingService.updateUserMapping(id, updatedUserMapping);
		return (userMapping != null) ? ResponseEntity.ok(userMapping) : ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUserMapping(@PathVariable UUID id) {
		userMappingService.deleteUserMapping(id);
		return ResponseEntity.noContent().build();
	}
}
