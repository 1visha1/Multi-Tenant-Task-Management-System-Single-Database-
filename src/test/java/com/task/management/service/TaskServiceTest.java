package com.task.management.service;

import com.task.management.exception.TaskNotFoundException;
import com.task.management.exception.UnauthorizedActionException;
import com.task.management.exception.UserNotFoundException;
import com.task.management.io.CustomUserDetails;
import com.task.management.io.NewTask;
import com.task.management.model.Task;
import com.task.management.model.User;
import com.task.management.repository.TaskRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private CustomUserDetails mockUser;


    @BeforeEach
    void setup() {
        mockUser = new CustomUserDetails(
                "test@example.com",
                "password",
                1,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private void mockSecurityContext() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void shouldCreateNewTask() {
        mockSecurityContext();

        NewTask newTask = new NewTask();
        newTask.setTitle("Test Task");
        newTask.setDescription("Desc");
        newTask.setStatus("OPEN");

        taskService.newTask(newTask);

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void shouldThrowExceptionWhenNewTaskIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.newTask(null));
    }

    @Test
    void shouldReturnTaskList() {
        mockSecurityContext();

        List<Task> tasks = List.of(new Task());
        when(taskRepository.findByTenantId(1)).thenReturn(tasks);

        List<Task> result = taskService.listTask();

        assertEquals(1, result.size());
    }

    @Test
    void shouldAssignTask() {
        mockSecurityContext();

        Task task = Task.builder()
                .id(1)
                .tenantId(1)
                .build();

        when(userService.isUserExists(10)).thenReturn(true);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        taskService.assignTask(10, 1);

        assertEquals(10, task.getAssignedTo());
        verify(taskRepository).save(task);
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        mockSecurityContext();

        when(userService.isUserExists(10)).thenReturn(true);
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.assignTask(10, 1));
    }

    @Test
    void shouldThrowWhenCrossTenantAssignment() {
        mockSecurityContext();

        Task task = Task.builder()
                .id(1)
                .tenantId(99)
                .build();

        when(userService.isUserExists(10)).thenReturn(true);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedActionException.class,
                () -> taskService.assignTask(10, 1));
    }

    @Test
    void shouldUpdateStatus() {
        mockSecurityContext();

        Task task = Task.builder()
                .id(1)
                .tenantId(1)
                .assignedTo(5)
                .build();

        User user = new User();
        user.setId(5);

        when(userService.findByUserEmail("test@example.com")).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        taskService.updateStatus(1, "DONE");

        assertEquals("DONE", task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void shouldThrowWhenUserNotAssigned() {
        mockSecurityContext();

        Task task = Task.builder()
                .id(1)
                .tenantId(1)
                .assignedTo(99)
                .build();

        User user = new User();
        user.setId(5);

        when(userService.findByUserEmail("test@example.com")).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedActionException.class,
                () -> taskService.updateStatus(1, "DONE"));
    }

    @Test
    void shouldThrowWhenUserNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(UserNotFoundException.class,
                () -> taskService.listTask());
    }
}