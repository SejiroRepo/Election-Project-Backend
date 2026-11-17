package com.ellectronic.voting.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentLoginRequest(

  @NotBlank(message = "Matric number is required")
  @Size(max = 100)
  @Pattern(
    regexp = "^[A-Za-z0-9/-]+$",
    message = "Matric number contains invalid characters"
  )
  String matricNumber,

  @NotBlank(message = "Password is required")
  String password
) {
}