package com.ellectronic.voting.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminLoginRequest(

  @NotBlank(message = "Email is required")
  @Email(message = "Enter a valid email address")
  @Size(max = 150)
  String email,

  @NotBlank(message = "Password is required")
  String password
) {
}