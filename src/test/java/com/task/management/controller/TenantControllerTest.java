package com.task.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.management.io.JWTRequest;
import com.task.management.io.JWTResponse;
import com.task.management.io.RegistrationIo;
import com.task.management.security.JwtHelper;
import com.task.management.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
@AutoConfigureMockMvc(addFilters = false)   // ✅ IMPORTANT
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtHelper jwtHelper;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------- REGISTER ----------------

    @Test
    void shouldRegisterTenant() throws Exception {

        RegistrationIo request = new RegistrationIo();

        doNothing().when(authService).register(any(RegistrationIo.class));

        mockMvc.perform(post("/api/tenant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Tenant and admin registered successfully"));
    }

    // ---------------- LOGIN ----------------

    @Test
    void shouldLoginUserAndReturnJwtToken() throws Exception {

        JWTRequest request = new JWTRequest();
        request.setEmail("test@mail.com");
        request.setPassword("1234");

        UserDetails userDetails =
                new User("test@mail.com", "1234", Collections.emptyList());

        when(authService.doAuthenticate(anyString(), anyString()))
                .thenReturn(userDetails);

        when(jwtHelper.generateToken(any(UserDetails.class)))
                .thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.jwtToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("test@mail.com"));
    }
}