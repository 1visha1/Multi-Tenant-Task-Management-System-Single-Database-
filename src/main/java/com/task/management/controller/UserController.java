package com.task.management.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.task.management.io.NewUser;
import com.task.management.io.SuccessResponse;
import com.task.management.model.User;
import com.task.management.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping
    public ResponseEntity<SuccessResponse<String>> addNewUser(@RequestBody NewUser newUser) {

        userService.addNewUser(newUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.<String>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("User created successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build());
    }


    @GetMapping
    public ResponseEntity<SuccessResponse<List<User>>> listUsers() {

        List<User> users = userService.listUser();

        return ResponseEntity.ok(
                SuccessResponse.<List<User>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Users fetched successfully")
                        .data(users)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}