package com.ellectronic.voting.auth.support;

import com.ellectronic.voting.common.exception.BusinessRuleException;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {

  public static final int MIN_LENGTH = 8;
  public static final int MAX_LENGTH = 72;

  private PasswordPolicy() {
  }

  public static void validate(String password) {
    if (password == null || password.isBlank()) {
      throw new BusinessRuleException("Password is required.");
    }

    if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
      throw new BusinessRuleException("Password must be between 8 and 72 characters.");
    }

    if (password.getBytes(StandardCharsets.UTF_8).length > MAX_LENGTH) {
      throw new BusinessRuleException("Password must not exceed 72 UTF-8 bytes.");
    }
  }
}