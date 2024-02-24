package com.id.sales.service.model;

import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

public abstract class BaseModel {

	private Date createdTime;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column(name = "created_time", nullable = true, insertable = true, updatable = false)
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    private String createdBy;

    @Column(name = "created_by", nullable = true, insertable = true, updatable = false, length = 25, precision = 0)
    @CreatedBy
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    private Date updatedTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_time", nullable = true, insertable = true, updatable = true)
    @LastModifiedDate
    @CreatedDate
    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    private String updatedBy;

    @Column(name = "updated_by", nullable = true, insertable = true, updatable = true, length = 25, precision = 0)
    @LastModifiedBy
    @CreatedBy
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
