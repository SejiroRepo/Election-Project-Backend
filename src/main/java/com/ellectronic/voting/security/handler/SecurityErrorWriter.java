package com.ellectronic.voting.security.handler;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;

public final class SecurityErrorWriter {

  private SecurityErrorWriter() {
  }

  public static void write(
    HttpServletResponse response,
    HttpStatus status,
    String message
  ) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(
      "{\"success\":false,\"message\":"
        + quote(message)
        + ",\"data\":null,\"timestamp\":"
        + quote(LocalDateTime.now().toString())
        + "}"
    );
  }

  private static String quote(String value) {
    String safe = value == null ? "" : value
      .replace("\\", "\\\\")
      .replace("\"", "\\\"");
    return "\"" + safe + "\"";
  }
}