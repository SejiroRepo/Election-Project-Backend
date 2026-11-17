package com.ellectronic.voting.auth.dto;

import com.ellectronic.voting.common.enums.AccountStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudentRegistrationResponse(
  UUID userId,
  String matricNumber,
  String email,
  AccountStatus accountStatus,
  LocalDateTime createdAt,
  String message
) {
}