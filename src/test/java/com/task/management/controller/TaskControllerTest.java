package com.task.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.management.io.NewTask;
import com.task.management.model.Task;
import com.task.management.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)   // ✅ IMPORTANT FIX
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;
    @MockBean
    private com.task.management.security.JwtHelper jwtHelper;

    @MockBean
    private com.task.management.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateNewTask() throws Exception {

        NewTask newTask = new NewTask();
        // set fields if required

        doNothing().when(taskService).newTask(Mockito.any(NewTask.class));

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Task created successfully"));
    }

    @Test
    void shouldReturnListOfTasks() throws Exception {

        Task task1 = new Task();
        Task task2 = new Task();

        List<Task> taskList = Arrays.asList(task1, task2);

        when(taskService.listTask()).thenReturn(taskList);

        mockMvc.perform(get("/api/task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Tasks fetched successfully"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldAssignTaskToUser() throws Exception {

        doNothing().when(taskService).assignTask(1, 10);

        mockMvc.perform(put("/api/task/1/assigne")
                        .param("taskId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Task assigned successfully"));
    }

    @Test
    void shouldUpdateTaskStatus() throws Exception {

        doNothing().when(taskService).updateStatus(1, "DONE");

        mockMvc.perform(put("/api/task/1/status")
                        .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Task status updated successfully"));
    }
}