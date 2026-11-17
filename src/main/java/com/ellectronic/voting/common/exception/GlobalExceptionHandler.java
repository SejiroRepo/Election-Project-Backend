package com.ellectronic.voting.common.exception;

import com.ellectronic.voting.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException exception) {
    String message = exception.getMessage() == null || exception.getMessage().isBlank()
      || "Bad credentials".equalsIgnoreCase(exception.getMessage())
      ? "Invalid credentials"
      : exception.getMessage();

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.failure(message));
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException exception) {
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.failure(exception.getMessage()));
  }

  @ExceptionHandler(LockedException.class)
  public ResponseEntity<ApiResponse<Void>> handleLocked(LockedException exception) {
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.failure(exception.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
    String message = exception.getMessage();
    if (message == null || message.isBlank() || "Access Denied".equalsIgnoreCase(message)) {
      message = "You do not have permission to perform this action.";
    }

    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ApiResponse.failure(message));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException exception) {
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.failure("Authentication failed"));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException exception) {
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.failure("Invalid request body"));
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
