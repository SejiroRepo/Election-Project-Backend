package com.ellectronic.voting.auth.controller;

import com.ellectronic.voting.auth.dto.AdminLoginRequest;
import com.ellectronic.voting.auth.dto.FirstLoginPasswordChangeRequest;
import com.ellectronic.voting.auth.dto.LoginResponse;
import com.ellectronic.voting.auth.dto.StudentLoginRequest;
import com.ellectronic.voting.auth.dto.StudentRegistrationRequest;
import com.ellectronic.voting.auth.dto.StudentRegistrationResponse;
import com.ellectronic.voting.auth.service.AuthenticationService;
import com.ellectronic.voting.auth.service.StudentRegistrationService;
import com.ellectronic.voting.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final StudentRegistrationService registrationService;
  private final AuthenticationService authenticationService;

  public AuthController(
    StudentRegistrationService registrationService,
    AuthenticationService authenticationService
  ) {
    this.registrationService = registrationService;
    this.authenticationService = authenticationService;
  }

  @PostMapping(
    value = "/student-register",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<StudentRegistrationResponse> register(
    @Valid @RequestPart("request") StudentRegistrationRequest request,
    @RequestPart("schoolIdImage") MultipartFile schoolIdImage
  ) {
    StudentRegistrationResponse response = registrationService.register(request, schoolIdImage);
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(response);
  }

  @PostMapping("/student-login")
  public ResponseEntity<ApiResponse<LoginResponse>> studentLogin(
    @Valid @RequestBody StudentLoginRequest request
  ) {
    LoginResponse response = authenticationService.loginStudent(request);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
  }

  @PostMapping("/admin-login")
  public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(
    @Valid @RequestBody AdminLoginRequest request
  ) {
    LoginResponse response = authenticationService.loginAdmin(request);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
  }

  @PutMapping("/change-password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
    @Valid @RequestBody FirstLoginPasswordChangeRequest request,
    Authentication authentication
  ) {
    authenticationService.changePassword(authentication, request);
    return ResponseEntity.ok(ApiResponse.success("Password updated successfully"));
  }
}