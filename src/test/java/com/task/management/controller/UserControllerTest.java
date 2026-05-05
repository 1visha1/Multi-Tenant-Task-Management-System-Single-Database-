package com.task.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.management.io.NewUser;
import com.task.management.model.User;
import com.task.management.security.JwtAuthenticationFilter;
import com.task.management.security.JwtHelper;
import com.task.management.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)   // ✅ IMPORTANT (fixes JWT/Security issue)
class UserControllerTest {
	@MockBean
	private JwtHelper jwtHelper;

	@MockBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------- CREATE USER ----------------

    @Test
    void shouldCreateNewUser() throws Exception {

        NewUser newUser = new NewUser();
        // set fields if required

        doNothing().when(userService).addNewUser(any(NewUser.class));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }

    // ---------------- LIST USERS ----------------

    @Test
    void shouldReturnListOfUsers() throws Exception {

        User user1 = new User();
        User user2 = new User();

        List<User> users = Arrays.asList(user1, user2);

        when(userService.listUser()).thenReturn(users);

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Users fetched successfully"))
                .andExpect(jsonPath("$.data").isArray());
    }
}