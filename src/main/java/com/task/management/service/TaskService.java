package com.task.management.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.task.management.exception.UserNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.task.management.repository.TaskRepository;
import com.task.management.exception.TaskNotFoundException;
import com.task.management.exception.UnauthorizedActionException;
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

    // Fix 3: Allowlist for valid status values
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("TODO", "IN_PROGRESS", "DONE", "CANCELLED");

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("User not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new UserNotFoundException("Invalid user context");
        }

        return (CustomUserDetails) authentication.getPrincipal();
    }

    public void newTask(NewTask newTask) {

        if (newTask == null) {
            throw new IllegalArgumentException("Task request cannot be null");
        }

        if (newTask.getTitle() == null || newTask.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title is required");
        }

        if (newTask.getStatus() == null || newTask.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Task status is required");
        }

        // Fix 3: Validate status against allowlist
        if (!ALLOWED_STATUSES.contains(newTask.getStatus().trim().toUpperCase())) {
            throw new IllegalArgumentException("Invalid status value: " + newTask.getStatus());
        }

        CustomUserDetails currentUser = getCurrentUser();

        Task task = Task.builder()
                .tenantId(currentUser.getTenantId())
                .title(newTask.getTitle().trim())
                .description(newTask.getDescription() != null ? newTask.getDescription().trim() : null)
                .status(newTask.getStatus().trim().toUpperCase())
                .build();

        taskRepository.save(task);
        log.info("New task created for tenantId: {}", currentUser.getTenantId());
    }

    public List<Task> listTask() {

        CustomUserDetails currentUser = getCurrentUser();

        List<Task> tasks = taskRepository.findByTenantId(currentUser.getTenantId());

        // Fix 2: Return empty list instead of null
        if (tasks == null || tasks.isEmpty()) {
            log.warn("No tasks found for tenantId: {}", currentUser.getTenantId());
            return Collections.emptyList();
        }

        return tasks;
    }

    public void assignTask(Integer userId, Integer taskId) {

        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }

        if (taskId == null) {
            throw new IllegalArgumentException("TaskId cannot be null");
        }

        if (!userService.isUserExists(userId)) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        CustomUserDetails currentUser = getCurrentUser();

        // Fix 1: Verify target user belongs to the same tenant
        User targetUser = userService.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!targetUser.getTenantId().equals(currentUser.getTenantId())) {
            throw new UnauthorizedActionException("Cross-tenant user assignment not allowed");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!task.getTenantId().equals(currentUser.getTenantId())) {
            throw new UnauthorizedActionException("Cross-tenant task assignment not allowed");
        }

        if (task.getAssignedTo() != null && task.getAssignedTo().equals(userId)) {
            log.warn("Task {} is already assigned to user {}", taskId, userId);
            return;
        }

        task.setAssignedTo(userId);
        taskRepository.save(task);

        log.info("Task {} assigned to user {}", taskId, userId);
    }

    public void updateStatus(Integer taskId, String status) {

        if (taskId == null) {
            throw new IllegalArgumentException("TaskId cannot be null");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        // Fix 3: Validate status against allowlist
        if (!ALLOWED_STATUSES.contains(status.trim().toUpperCase())) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        CustomUserDetails currentUser = getCurrentUser();

        User user = userService.findByUserEmail(currentUser.getUsername());
        if (user == null) {
            throw new UserNotFoundException("Logged-in user not found");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!task.getTenantId().equals(currentUser.getTenantId())) {
            throw new UnauthorizedActionException("Cross-tenant access denied");
        }

        if (task.getAssignedTo() == null) {
            throw new UnauthorizedActionException("Task is not assigned to any user");
        }

        if (!user.getId().equals(task.getAssignedTo())) {
            throw new UnauthorizedActionException("You are not allowed to update this task");
        }

        task.setStatus(status.trim().toUpperCase());
        taskRepository.save(task);

        log.info("Task {} status updated to {}", taskId, status);
    }
}