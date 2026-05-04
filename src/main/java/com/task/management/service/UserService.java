package com.task.management.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.task.management.exception.UserNotFoundException;
import com.task.management.io.CustomUserDetails;
import com.task.management.io.NewUser;
import com.task.management.model.User;
import com.task.management.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	
	public void addNewUser(NewUser newUser) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

		Integer tenantId = currentUser.getTenantId();
		User user = User.builder()
				.emailId(newUser.getEmailId())
				.role(newUser.getRole())
				.password(passwordEncoder.encode(newUser.getPassword()))
				.tenantId(tenantId)
				.build();
		userRepository.save(user);
	}
	
	public List<User> listUser(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

		Integer tenantId = currentUser.getTenantId();
		
		List<User> users = userRepository.findByTenantId(tenantId);
		return users;
	}
	
	public boolean isUserExists(Integer id) {
	    return userRepository.findById(id).isPresent();
	}

	public User findByUserEmail(String email) {
	    return userRepository.findByEmailId(email)
	            .orElseThrow(() -> new UserNotFoundException("User not found!"));
	}

}
