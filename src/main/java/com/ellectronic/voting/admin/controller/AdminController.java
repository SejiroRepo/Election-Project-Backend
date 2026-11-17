package com.ellectronic.voting.admin.controller;

import com.ellectronic.voting.auth.dto.AdminCreationRequest;
import com.ellectronic.voting.auth.dto.AdminCreationResponse;
import com.ellectronic.voting.auth.service.AdminProvisioningService;
import com.ellectronic.voting.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

  private final AdminProvisioningService adminProvisioningService;

  public AdminController(AdminProvisioningService adminProvisioningService) {
    this.adminProvisioningService = adminProvisioningService;
  }

  @PostMapping
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<AdminCreationResponse>> create(
    @Valid @RequestBody AdminCreationRequest request
  ) {
    AdminCreationResponse response = adminProvisioningService.createAdministrator(request);
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(ApiResponse.success("Administrator created successfully", response));
  }
}