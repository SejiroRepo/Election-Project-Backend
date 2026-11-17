package com.ellectronic.voting.auth.controller;

import com.ellectronic.voting.auth.dto.AdminLoginRequest;
import com.ellectronic.voting.auth.dto.AuthenticatedUserResponse;
import com.ellectronic.voting.auth.dto.LoginResponse;
import com.ellectronic.voting.auth.dto.StudentLoginRequest;
import com.ellectronic.voting.auth.service.AuthenticationService;
import com.ellectronic.voting.auth.service.StudentRegistrationService;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private StudentRegistrationService registrationService;

  @Mock
  private AuthenticationService authenticationService;

  private MockMvc mockMvc;
  private JsonMapper jsonMapper;

  @BeforeEach
  void setUp() {
    jsonMapper = JsonMapper.builder().build();
    mockMvc = MockMvcBuilders.standaloneSetup(
        new AuthController(registrationService, authenticationService)
      )
      .setControllerAdvice(new GlobalExceptionHandler())
      .setMessageConverters(new JacksonJsonHttpMessageConverter(jsonMapper))
      .build();
  }

  @Test
  void studentLoginReturnsToken() throws Exception {
    LoginResponse response = LoginResponse.bearer(
      "jwt-token",
      new AuthenticatedUserResponse(
        UUID.randomUUID(),
        "Ada",
        "Okeke",
        "ada@example.com",
        "STU/1234",
        AccountStatus.ACTIVE,
        false,
        Set.of("STUDENT"),
        Set.of("STUDENT_VIEW")
      )
    );
    when(authenticationService.loginStudent(any(StudentLoginRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/student-login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(new StudentLoginRequest("STU/1234", "Password1"))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
      .andExpect(jsonPath("$.data.user.roles").isArray())
      .andExpect(jsonPath("$.data.user.matricNumber").value("STU/1234"));
  }

  @Test
  void adminLoginReturnsToken() throws Exception {
    LoginResponse response = LoginResponse.bearer(
      "admin-token",
      new AuthenticatedUserResponse(
        UUID.randomUUID(),
        "Ada",
        "Admin",
        "admin@example.com",
        null,
        AccountStatus.ACTIVE,
        true,
        Set.of("ADMIN"),
        Set.of("STUDENT_APPROVE")
      )
    );
    when(authenticationService.loginAdmin(any(AdminLoginRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/admin-login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(new AdminLoginRequest("admin@example.com", "Password1"))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.accessToken").value("admin-token"))
      .andExpect(jsonPath("$.data.user.passwordChangeRequired").value(true));
  }

  @Test
  void studentLoginMapsInvalidCredentialsToUnauthorized() throws Exception {
    when(authenticationService.loginStudent(any(StudentLoginRequest.class)))
      .thenThrow(new BadCredentialsException("Bad credentials"));

    mockMvc.perform(post("/api/auth/student-login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(new StudentLoginRequest("STU/1234", "wrong"))))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.message").value("Invalid credentials"));
  }

  @Test
  void studentLoginMapsPendingAccountToUnauthorized() throws Exception {
    when(authenticationService.loginStudent(any(StudentLoginRequest.class)))
      .thenThrow(new DisabledException("Your registration is awaiting administrator verification."));

    mockMvc.perform(post("/api/auth/student-login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(new StudentLoginRequest("STU/1234", "Password1"))))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.message").value("Your registration is awaiting administrator verification."));
  }
}
