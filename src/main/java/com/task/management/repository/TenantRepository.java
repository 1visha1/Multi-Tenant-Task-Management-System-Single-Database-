package com.task.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.task.management.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Integer>{

}
