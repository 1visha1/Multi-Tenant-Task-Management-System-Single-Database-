package com.task.management.service;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.task.management.io.CustomUserDetails;
import com.task.management.model.User;
import com.task.management.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomeUserDetailsService implements UserDetailsService {

   
    private final UserRepository userRepository;
    


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmailId(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        log.info("Loading user by email: {}", email);

        return new CustomUserDetails(
                user.getEmailId(),
                user.getPassword(),
                user.getTenantId(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole())
                )
        );
    }
}
