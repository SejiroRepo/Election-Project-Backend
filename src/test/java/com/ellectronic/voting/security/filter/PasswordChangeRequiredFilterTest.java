package com.ellectronic.voting.security.filter;

import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.security.user.AppUserDetails;
import com.ellectronic.voting.testsupport.TestUsers;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PasswordChangeRequiredFilterTest {

  private PasswordChangeRequiredFilter filter;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    filter = new PasswordChangeRequiredFilter();
    filterChain = mock(FilterChain.class);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void blocksProtectedEndpointsWhenPasswordChangeIsRequired() throws Exception {
    AppUserDetails principal = AppUserDetails.from(
      TestUsers.admin(AccountStatus.ACTIVE, "hash", true),
      "admin@example.com"
    );
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
    );

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/elections");
    request.setServletPath("/api/elections");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(response.getContentAsString()).contains("Password change required");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void allowsPasswordChangeEndpoint() throws Exception {
    AppUserDetails principal = AppUserDetails.from(
      TestUsers.admin(AccountStatus.ACTIVE, "hash", true),
      "admin@example.com"
    );
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
    );

    MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/auth/change-password");
    request.setServletPath("/api/auth/change-password");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void allowsProtectedEndpointsAfterPasswordHasBeenChanged() throws Exception {
    AppUserDetails principal = AppUserDetails.from(
      TestUsers.admin(AccountStatus.ACTIVE, "hash", false),
      "admin@example.com"
    );
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
    );

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/elections");
    request.setServletPath("/api/elections");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }
}
