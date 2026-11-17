package com.ellectronic.voting.auth.support;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TemporaryPasswordGeneratorTest {

  @Test
  void generatesExactlyEightNumericDigits() {
    TemporaryPasswordGenerator generator = new TemporaryPasswordGenerator();
    Set<String> generated = new HashSet<>();

    for (int i = 0; i < 200; i++) {
      String password = generator.generateEightDigitPassword();
      assertThat(password).matches("\\d{8}");
      generated.add(password);
    }

    assertThat(generated.size()).isGreaterThan(1);
  }
}
