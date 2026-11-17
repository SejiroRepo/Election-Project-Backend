package com.ellectronic.voting.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record StudentRegistrationRequest(

  @NotBlank(message = "First name is required")
  @Size(max = 100)
  String firstName,

  @NotBlank(message = "Last name is required")
  @Size(max = 100)  
  String lastName,

  @NotBlank(message = "Matric number is required")
  @Size(max = 100)
  @Pattern(
    regexp = "^[A-Za-z0-9/-]+$",
    message = "Matric number contains invalid characters"
  )
  String matricNumber,

  @NotBlank(message = "Email is required")
  @Email(message = "Enter a valid email address")
  @Size(max = 150)
  String email,


  @NotBlank(message = "Password is required")
  @Size(
    min = 8,
    max = 72,
    message = "Password must be between 8 and 72 characters"
  )
  String password

) {
  
}
