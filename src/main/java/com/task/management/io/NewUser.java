package com.task.management.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Builder
public class NewUser {
	private String emailId;
	private String password;
	private String role;
	
}
