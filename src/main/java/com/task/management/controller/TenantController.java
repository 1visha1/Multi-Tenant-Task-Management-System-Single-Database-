package com.task.management.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.task.management.io.JWTRequest;
import com.task.management.io.JWTResponse;
import com.task.management.io.RegistrationIo;
import com.task.management.io.SuccessResponse;
import com.task.management.security.JwtHelper;
import com.task.management.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class TenantController {

    private final AuthService authService;
    private final JwtHelper helper;
    
    @PostMapping("/tenant/register")
    public ResponseEntity<SuccessResponse<String>> register(@RequestBody RegistrationIo registrationIo) {

        authService.register(registrationIo);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.<String>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Tenant and admin registered successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build());
    }


    @PostMapping("/auth/login")
    public ResponseEntity<SuccessResponse<JWTResponse>> login(@RequestBody JWTRequest jwtRequest) {

        UserDetails userDetails =
                authService.doAuthenticate(jwtRequest.getEmail(), jwtRequest.getPassword());

        String token = helper.generateToken(userDetails);

        log.info("Login successful for email: {}", jwtRequest.getEmail());

        JWTResponse response = JWTResponse.builder()
                .jwtToken(token)
                .username(userDetails.getUsername())
                .build();

        return ResponseEntity.ok(
                SuccessResponse.<JWTResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Login successful")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}