package com.ellectronic.voting.auth.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TemporaryPasswordGenerator {

  private static final int PASSWORD_LENGTH = 8;
  private static final int NUMERIC_BOUND = 100_000_000;

  private final SecureRandom secureRandom = new SecureRandom();

  public String generateEightDigitPassword() {
    int value = secureRandom.nextInt(NUMERIC_BOUND);
    return String.format("%0" + PASSWORD_LENGTH + "d", value);
  }
}