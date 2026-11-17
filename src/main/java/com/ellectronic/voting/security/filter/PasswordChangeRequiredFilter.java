package com.ellectronic.voting.security.filter;

import com.ellectronic.voting.security.exception.PasswordChangeRequiredException;
import com.ellectronic.voting.security.handler.SecurityErrorWriter;
import com.ellectronic.voting.security.user.AppUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
      || !authentication.isAuthenticated()
      || authentication instanceof AnonymousAuthenticationToken) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!(authentication.getPrincipal() instanceof AppUserDetails principal)
      || !principal.isPasswordChangeRequired()) {
      filterChain.doFilter(request, response);
      return;
    }

    if (isPasswordChangeRequest(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    SecurityErrorWriter.write(
      response,
      HttpStatus.FORBIDDEN,
      new PasswordChangeRequiredException().getMessage()
    );
  }

  private boolean isPasswordChangeRequest(HttpServletRequest request) {
    return HttpMethod.PUT.matches(request.getMethod())
      && "/api/auth/change-password".equals(request.getServletPath());
  }
}