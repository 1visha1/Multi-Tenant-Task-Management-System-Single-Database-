package com.task.management.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RegistrationIo {
	
	
	private String email;
	private String password;
	private String confirmpassword;
	private String tenantName;
	
}
