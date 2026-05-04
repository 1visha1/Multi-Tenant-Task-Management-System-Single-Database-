package com.task.management.exception;
//TaskNotFoundException
public class TaskNotFoundException extends RuntimeException {
 public TaskNotFoundException(String msg) {
     super(msg);
 }
}