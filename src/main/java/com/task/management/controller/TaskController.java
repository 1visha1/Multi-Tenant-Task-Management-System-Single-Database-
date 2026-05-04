package com.task.management.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.task.management.io.NewTask;
import com.task.management.io.SuccessResponse;
import com.task.management.model.Task;
import com.task.management.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;


    @PostMapping("/task")
    public ResponseEntity<SuccessResponse<String>> newTask(@RequestBody NewTask newTask) {

        taskService.newTask(newTask);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.<String>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Task created successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build());
    }


    @GetMapping("/task")
    public ResponseEntity<SuccessResponse<List<Task>>> listTasks() {

        List<Task> tasks = taskService.listTask();

        return ResponseEntity.ok(
                SuccessResponse.<List<Task>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Tasks fetched successfully")
                        .data(tasks)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PutMapping("/task/{id}/assigne")
    public ResponseEntity<SuccessResponse<String>> assignTaskToUser(
            @RequestParam Integer taskId,
            @PathVariable Integer id) {

        taskService.assignTask(id, taskId);

        return ResponseEntity.ok(
                SuccessResponse.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message("Task assigned successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }


    @PutMapping("/task/{id}/status")
    public ResponseEntity<SuccessResponse<String>> updateTaskStatus(
            @RequestParam String status,
            @PathVariable Integer id) {

        taskService.updateStatus(id, status);

        return ResponseEntity.ok(
                SuccessResponse.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message("Task status updated successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}