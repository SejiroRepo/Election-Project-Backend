package com.ellectronic.voting.auth.support;

import java.util.Locale;

public final class IdentifierNormalizer {

  private IdentifierNormalizer() {
  }

  public static String normalizeMatricNumber(String matricNumber) {
    if (matricNumber == null) {
      return null;
    }
    return matricNumber.trim().toUpperCase(Locale.ROOT);
  }

  public static String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }

  public static boolean looksLikeEmail(String identifier) {
    return identifier != null && identifier.contains("@");
  }
}