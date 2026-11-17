package com.ellectronic.voting.auth.dto;

public record LoginResponse(
  String accessToken,
  String tokenType,
  AuthenticatedUserResponse user
) {

  public static LoginResponse bearer(String accessToken, AuthenticatedUserResponse user) {
    return new LoginResponse(accessToken, "Bearer", user);
  }
}