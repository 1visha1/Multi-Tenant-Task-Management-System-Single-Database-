package com.task.management.service;

import com.task.management.exception.UserNotFoundException;
import com.task.management.io.CustomUserDetails;
import com.task.management.io.NewUser;
import com.task.management.model.User;
import com.task.management.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private CustomUserDetails mockUser;

    @BeforeEach
    void setup() {
        mockUser = new CustomUserDetails(
                "admin@test.com",
                "password",
                1,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext() {
        when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void shouldAddNewUser() {
        mockSecurityContext();

        NewUser newUser = new NewUser();
        newUser.setEmailId("user@test.com");
        newUser.setPassword("password");
        newUser.setRole("USER");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        userService.addNewUser(newUser);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldReturnUsersByTenant() {
        mockSecurityContext();

        List<User> users = List.of(new User(), new User());

        when(userRepository.findByTenantId(1)).thenReturn(users);

        List<User> result = userService.listUser();

        assertEquals(2, result.size());
        verify(userRepository).findByTenantId(1);
    }

    @Test
    void shouldReturnTrueIfUserExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));

        boolean result = userService.isUserExists(1);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseIfUserDoesNotExist() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        boolean result = userService.isUserExists(1);

        assertFalse(result);
    }

    @Test
    void shouldReturnUserByEmail() {
        User user = new User();
        user.setEmailId("test@test.com");

        when(userRepository.findByEmailId("test@test.com"))
                .thenReturn(Optional.of(user));

        User result = userService.findByUserEmail("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmailId());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmailId("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.findByUserEmail("missing@test.com"));
    }
}