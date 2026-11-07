package com.ellectronic.voting.common.exception;

import com.ellectronic.voting.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice 
public class GlobalExceptionHandler {
  
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException exception) {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ApiResponse.failure(exception.getMessage()));
  }

  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException exception) {
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.failure(exception.getMessage()));
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException exception) {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ApiResponse.failure(exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();

    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(
        error.getField(),
        error.getDefaultMessage()
      );
    }

    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.failure(
        "Validation failed",
        errors
      ));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.failure(exception.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception exception) {
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.failure("An unexpected error occurred"));
  }

}
