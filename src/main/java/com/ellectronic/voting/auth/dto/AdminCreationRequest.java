package com.ellectronic.voting.auth.dto;

import com.ellectronic.voting.common.enums.PermissionName;
import com.ellectronic.voting.common.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AdminCreationRequest(

  @NotBlank(message = "First name is required")
  @Size(max = 100)
  String firstName,

  @NotBlank(message = "Last name is required")
  @Size(max = 100)
  String lastName,

  @NotBlank(message = "Email is required")
  @Email(message = "Enter a valid email address")
  @Size(max = 150)
  String email,

  Set<RoleName> roles,

  Set<PermissionName> permissions
) {
}