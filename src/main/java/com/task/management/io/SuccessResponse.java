package com.task.management.io;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SuccessResponse<T> {

    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}