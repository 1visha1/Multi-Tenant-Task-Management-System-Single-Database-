package com.task.management.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.task.management.repository.TaskRepository;
import com.task.management.exception.TaskNotFoundException;
import com.task.management.exception.UnauthorizedActionException;
import com.task.management.exception.UserNotAuthenticatedException;
import com.task.management.io.CustomUserDetails;
import com.task.management.io.NewTask;
import com.task.management.model.Task;
import com.task.management.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {
	
	private final TaskRepository taskRepository;
	private final UserService userService;
	
	private CustomUserDetails getCurrentUser() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	    if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
	        throw new RuntimeException("User not authenticated");
	    }

	    return (CustomUserDetails) authentication.getPrincipal();
	}
	
	public void newTask(NewTask newTask) {
		
		

	    CustomUserDetails currentUser = getCurrentUser();

	    Task task = Task.builder()
	            .tenantId(currentUser.getTenantId())
	            .title(newTask.getTitle())
	            .description(newTask.getDescription())
	            .status(newTask.getStatus())
	            .build();

	    taskRepository.save(task);
	}

	public List<Task> listTask() {

	    CustomUserDetails currentUser = getCurrentUser();

	    return taskRepository.findByTenantId(currentUser.getTenantId());
	}
	
	public void assignTask(Integer userId, Integer taskId) {

	    if (!userService.isUserExists(userId)) {
	        throw new UsernameNotFoundException("User not exists");
	    }

	    Task task = taskRepository.findById(taskId)
	            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

	    task.setAssignedTo(userId);

	    taskRepository.save(task);
	}
	

	public void updateStatus(Integer taskId, String status) {

	    CustomUserDetails currentUser = getCurrentUser();

	    User user = userService.findByUserEmail(currentUser.getUsername());

	    Task task = taskRepository.findById(taskId)
	            .orElseThrow(() -> new RuntimeException("Task not found"));

	    if (!task.getTenantId().equals(currentUser.getTenantId())) {
	        throw new UnauthorizedActionException("Cross-tenant access denied");
	    }

	    if (!user.getId().equals(task.getAssignedTo())) {
	        throw new UnauthorizedActionException("You are not allowed to update this task");
	    }

	    task.setStatus(status);
	    taskRepository.save(task);
	}
	
}


