package com.ellectronic.voting.security.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordServiceTest {

  private final PasswordService passwordService = new PasswordService(new BCryptPasswordEncoder());

  @Test
  void encodesAndMatchesPasswordsWithBCrypt() {
    String encoded = passwordService.encode("Password1");

    assertThat(encoded).isNotEqualTo("Password1");
    assertThat(encoded).startsWith("$2");
    assertThat(passwordService.matches("Password1", encoded)).isTrue();
    assertThat(passwordService.matches("wrong", encoded)).isFalse();
  }
}
