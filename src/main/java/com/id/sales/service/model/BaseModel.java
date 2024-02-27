package com.id.sales.service.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "updated_by")
	private String updatedBy;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Date updatedAt;

	@Column(name = "created_by")
	private String createdBy;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Date createdAt;

	@PrePersist
	protected void onCreate() {
		try {
			Jwt user = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			this.createdBy = user.getClaimAsString("username");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@PreUpdate
	protected void onUpdate() {
		try {
			Jwt user = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			this.updatedBy = user.getClaimAsString("username");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
