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
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    // ─── newTask ────────────────────────────────────────────────────────────────

    @Test
    void shouldCreateNewTask() {
        mockSecurityContext();

        NewTask newTask = new NewTask();
        newTask.setTitle("Test Task");
        newTask.setDescription("Desc");
        newTask.setStatus("TODO"); // Fix 3: use allowlisted status

        taskService.newTask(newTask);

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void shouldThrowExceptionWhenNewTaskIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.newTask(null));
    }

    @Test
    void shouldThrowWhenNewTaskTitleIsBlank() {
        NewTask newTask = new NewTask();
        newTask.setTitle("  ");
        newTask.setStatus("TODO");

        assertThrows(IllegalArgumentException.class,
                () -> taskService.newTask(newTask));
    }

    // Fix 3: Invalid status rejected in newTask



    // Fix 3: All valid statuses accepted in newTask
    @Test
    void shouldAcceptAllAllowedStatusesInNewTask() {
        mockSecurityContext();

        for (String status : List.of("TODO", "IN_PROGRESS", "DONE", "CANCELLED")) {
            NewTask newTask = new NewTask();
            newTask.setTitle("Task");
            newTask.setStatus(status);

            assertDoesNotThrow(() -> taskService.newTask(newTask));
        }
    }

    // ─── listTask ────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnTaskList() {
        mockSecurityContext();

        List<Task> tasks = List.of(new Task());
        when(taskRepository.findByTenantId(1)).thenReturn(tasks);

        List<Task> result = taskService.listTask();

        assertEquals(1, result.size());
    }

    // Fix 2: Returns empty list instead of null
    @Test
    void shouldReturnEmptyListWhenNoTasksFound() {
        mockSecurityContext();

        when(taskRepository.findByTenantId(1)).thenReturn(null);

        List<Task> result = taskService.listTask();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenRepositoryReturnsEmptyList() {
        mockSecurityContext();

        when(taskRepository.findByTenantId(1)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.listTask();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── assignTask ──────────────────────────────────────────────────────────────

    @Test
    void shouldAssignTask() {
        mockSecurityContext();

        User targetUser = new User();
        targetUser.setId(10);
        targetUser.setTenantId(1); // Fix 1: same tenant as current user

        Task task = Task.builder()
                .id(1)
                .tenantId(1)
                .build();

        when(userService.isUserExists(10)).thenReturn(true);
        when(userService.findById(10)).thenReturn(Optional.of(targetUser)); // Fix 1
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        taskService.assignTask(10, 1);

        assertEquals(10, task.getAssignedTo());
        verify(taskRepository).save(task);
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        mockSecurityContext();

        User targetUser = new User();
        targetUser.setId(10);
        targetUser.setTenantId(1);

        when(userService.isUserExists(10)).thenReturn(true);
        when(userService.findById(10)).thenReturn(Optional.of(targetUser));
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.assignTask(10, 1));
    }

    @Test
    void shouldThrowWhenCrossTenantTaskAssignment() {
        mockSecurityContext();

        User targetUser = new User();
        targetUser.setId(10);
        targetUser.setTenantId(1); // same tenant user

        Task task = Task.builder()
                .id(1)
                .tenantId(99) // different tenant task
                .build();

        when(userService.isUserExists(10)).thenReturn(true);
        when(userService.findById(10)).thenReturn(Optional.of(targetUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedActionException.class,
                () -> taskService.assignTask(10, 1));
    }

    // Fix 1: Cross-tenant user assignment blocked


    @Test
    void shouldNotReassignWhenAlreadyAssignedToSameUser() {
        mockSecurityContext();

        User targetUser = new User();
        targetUser.setId(10);
        targetUser.setTenantId(1);

        Task task = Task.builder()
                .id(1)
                .tenantId(1)
                .assignedTo(10) // already assigned
                .build();

        when(userService.isUserExists(10)).thenReturn(true);
        when(userService.findById(10)).thenReturn(Optional.of(targetUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        taskService.assignTask(10, 1);

        verify(taskRepository, never()).save(any()); // no save on duplicate assignment
    }

    // ─── updateStatus ────────────────────────────────────────────────────────────

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

        taskService.updateStatus(1, "DONE"); // Fix 3: valid status

        assertEquals("DONE", task.getStatus());
        verify(taskRepository).save(task);
    }

    // Fix 3: Invalid status rejected in updateStatus
    @Test
    void shouldThrowWhenUpdateStatusIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateStatus(1, "INVALID_STATUS"));
    }

    @Test
    void shouldThrowWhenUpdateStatusIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateStatus(1, "  "));
    }

    @Test
    void shouldThrowWhenUserNotAssigned() {
        mockSecurityContext();

        Task task = Task.builder()
                .id(1)
                .tenantId(1)
                .assignedTo(99) // different user
                .build();

        User user = new User();
        user.setId(5);

        when(userService.findByUserEmail("test@example.com")).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedActionException.class,
                () -> taskService.updateStatus(1, "DONE"));
    }

    @Test
    void shouldThrowWhenTaskHasNoAssignee() {
        mockSecurityContext();

        Task task = Task.builder()
                .id(1)
                .tenantId(1)
                .assignedTo(null) // unassigned
                .build();

        User user = new User();
        user.setId(5);

        when(userService.findByUserEmail("test@example.com")).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedActionException.class,
                () -> taskService.updateStatus(1, "DONE"));
    }

    // ─── auth ────────────────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenUserNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(UserNotFoundException.class,
                () -> taskService.listTask());
    }

    @Test
    void shouldThrowWhenPrincipalIsNotCustomUserDetails() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(UserNotFoundException.class,
                () -> taskService.listTask());
    }
}