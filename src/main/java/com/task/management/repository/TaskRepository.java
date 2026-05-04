package com.task.management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.task.management.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer>{

	List<Task> findByTenantId(Integer tenantId);

}
