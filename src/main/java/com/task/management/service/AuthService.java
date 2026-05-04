package com.task.management.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.task.management.exception.AuthenticationFailedException;
import com.task.management.exception.PasswordMismatchException;
import com.task.management.exception.UserAlreadyExistsException;
import com.task.management.exception.UserNotFoundException;
import com.task.management.io.RegistrationIo;
import com.task.management.model.Tenant;
import com.task.management.model.User;
import com.task.management.repository.TenantRepository;
import com.task.management.repository.UserRepository;

import java.util.Date;
import java.util.UUID;

import org.hibernate.id.UUIDGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TenantRepository tenantRepository;
    private final AuthenticationManager manager;
    
    public void register(RegistrationIo registrationIo) {

        if (!registrationIo.getPassword().equals(registrationIo.getConfirmpassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }


        userRepository.findByEmailId(registrationIo.getEmail())
                .ifPresent(u -> {
                    throw new UserAlreadyExistsException("User already exists with email: " + registrationIo.getEmail());
                });

        Tenant tenant = Tenant.builder()
                .name(registrationIo.getTenantName())
                .createdAt(new Date())
                .build();

        tenant = tenantRepository.save(tenant);

        User user = User.builder()
                .emailId(registrationIo.getEmail())
                .password(passwordEncoder.encode(registrationIo.getPassword()))
                .role("ADMIN")
                .tenantId(tenant.getId())
                .build();

        userRepository.save(user);

        log.info("Admin registered {}", user);
    }
    
    
    public UserDetails doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, password);

        try {
            Authentication auth = manager.authenticate(authentication);
            return (UserDetails) auth.getPrincipal();

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for email: {}", email);
            throw new AuthenticationFailedException("Invalid username or password");
        }
    }
}
