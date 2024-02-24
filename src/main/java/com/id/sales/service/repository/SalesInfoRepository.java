package com.id.sales.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.id.sales.service.model.SalesInfo;

public interface SalesInfoRepository extends JpaRepository<SalesInfo, Integer>,QuerydslPredicateExecutor<SalesInfo>{

}
