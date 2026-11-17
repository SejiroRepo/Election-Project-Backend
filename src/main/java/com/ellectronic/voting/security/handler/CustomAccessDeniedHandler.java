package com.ellectronic.voting.security.handler;

import com.ellectronic.voting.security.exception.PasswordChangeRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
    HttpServletRequest request,
    HttpServletResponse response,
    AccessDeniedException accessDeniedException
  ) throws IOException {
    String message = accessDeniedException instanceof PasswordChangeRequiredException
      ? accessDeniedException.getMessage()
      : "You do not have permission to perform this action.";

    SecurityErrorWriter.write(
      response,
      HttpStatus.FORBIDDEN,
      message
    );
  }
}