package com.ellectronic.voting.auth.dto;

import com.ellectronic.voting.common.enums.AccountStatus;

import java.util.Set;
import java.util.UUID;

public record AdminCreationResponse(
  UUID id,
  String firstName,
  String lastName,
  String email,
  Set<String> roles,
  Set<String> permissions,
  AccountStatus accountStatus,
  String temporaryPassword,
  boolean passwordChangeRequired
) {
}