package com.task.management.model;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
public class Task {
	@Id
	@GeneratedValue
	private Integer id;
	private String title;
	private String description;
	private String status;
	private Integer assignedTo;
	
	@Column(nullable = false)
	private Integer tenantId;
	
}
