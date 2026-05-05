package com.task.management.service;

import com.task.management.exception.AuthenticationFailedException;
import com.task.management.exception.PasswordMismatchException;
import com.task.management.exception.UserAlreadyExistsException;
import com.task.management.io.RegistrationIo;
import com.task.management.model.Tenant;
import com.task.management.model.User;
import com.task.management.repository.TenantRepository;
import com.task.management.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager manager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegistrationIo registrationIo;

    @BeforeEach
    void setup() {
        registrationIo = new RegistrationIo();
        registrationIo.setEmail("test@example.com");
        registrationIo.setPassword("password");
        registrationIo.setConfirmpassword("password");
        registrationIo.setTenantName("TestTenant");
    }


    @Test
    void shouldRegisterUserSuccessfully() {

        when(userRepository.findByEmailId("test@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password"))
                .thenReturn("encodedPassword");

        Tenant savedTenant = Tenant.builder().id(1).build();
        when(tenantRepository.save(any(Tenant.class)))
                .thenReturn(savedTenant);

        authService.register(registrationIo);

        verify(tenantRepository).save(any(Tenant.class));
        verify(userRepository).save(any(User.class));
    }


    @Test
    void shouldThrowWhenPasswordsDoNotMatch() {
        registrationIo.setConfirmpassword("wrong");

        assertThrows(PasswordMismatchException.class,
                () -> authService.register(registrationIo));
    }


    @Test
    void shouldThrowWhenUserAlreadyExists() {

        when(userRepository.findByEmailId("test@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registrationIo));
    }


    @Test
    void shouldAuthenticateSuccessfully() {

        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetails result = authService.doAuthenticate("test@example.com", "password");

        assertNotNull(result);
        assertEquals(userDetails, result);
    }


    @Test
    void shouldThrowWhenBadCredentials() {

        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthenticationFailedException.class,
                () -> authService.doAuthenticate("test@example.com", "wrong"));
    }
}