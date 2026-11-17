package com.ellectronic.voting.security.exception;

import org.springframework.security.access.AccessDeniedException;

public class PasswordChangeRequiredException extends AccessDeniedException {

  public PasswordChangeRequiredException() {
    super("Password change required. Please change your password before continuing.");
  }
}