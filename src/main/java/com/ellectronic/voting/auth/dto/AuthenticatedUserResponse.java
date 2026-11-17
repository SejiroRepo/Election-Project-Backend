package com.ellectronic.voting.auth.dto;

import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.security.user.AppUserDetails;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUserResponse(
  UUID id,
  String firstName,
  String lastName,
  String email,
  String matricNumber,
  AccountStatus accountStatus,
  boolean passwordChangeRequired,
  Set<String> roles,
  Set<String> permissions
) {

  public static AuthenticatedUserResponse from(AppUserDetails user) {
    return new AuthenticatedUserResponse(
      user.getId(),
      user.getFirstName(),
      user.getLastName(),
      user.getEmail(),
      user.getMatricNumber(),
      user.getAccountStatus(),
      user.isPasswordChangeRequired(),
      user.getRoles(),
      user.getPermissions()
    );
  }
}