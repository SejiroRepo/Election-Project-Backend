package com.ellectronic.voting.auth.service;

import com.ellectronic.voting.auth.dto.AdminLoginRequest;
import com.ellectronic.voting.auth.dto.FirstLoginPasswordChangeRequest;
import com.ellectronic.voting.auth.dto.LoginResponse;
import com.ellectronic.voting.auth.dto.StudentLoginRequest;
import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.exception.BusinessRuleException;
import com.ellectronic.voting.security.jwt.JwtService;
import com.ellectronic.voting.security.service.PasswordService;
import com.ellectronic.voting.security.user.AppUserDetails;
import com.ellectronic.voting.testsupport.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtService jwtService;

  @Mock
  private PasswordService passwordService;

  @Mock
  private UserRepository userRepository;

  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    authenticationService = new AuthenticationService(
      authenticationManager,
      jwtService,
      passwordService,
      userRepository
    );
  }

  @Test
  void studentLoginSucceedsForActiveAccount() {
    User student = TestUsers.student(AccountStatus.ACTIVE, "hash");
    AppUserDetails principal = AppUserDetails.from(student, "STU/1234");
    stubSuccessfulAuthentication(principal);
    when(jwtService.generateAccessToken(principal)).thenReturn("jwt-token");

    LoginResponse response = authenticationService.loginStudent(
      new StudentLoginRequest("stu/1234", "Password1")
    );

    ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
    verify(authenticationManager).authenticate(captor.capture());
    assertThat(captor.getValue().getPrincipal()).isEqualTo("STU/1234");
    assertThat(response.accessToken()).isEqualTo("jwt-token");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.user().roles()).contains("STUDENT");
    assertThat(response.user().matricNumber()).isEqualTo("STU/1234");
  }

  @Test
  void studentLoginRejectsInvalidPassword() {
    when(authenticationManager.authenticate(any()))
      .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authenticationService.loginStudent(
      new StudentLoginRequest("STU/1234", "wrong")
    )).isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void studentLoginRejectsUnknownMatricNumber() {
    when(authenticationManager.authenticate(any()))
      .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authenticationService.loginStudent(
      new StudentLoginRequest("UNKNOWN/000", "Password1")
    )).isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void studentLoginRejectsPendingAccount() {
    when(authenticationManager.authenticate(any()))
      .thenThrow(new DisabledException("Your registration is awaiting administrator verification."));

    assertThatThrownBy(() -> authenticationService.loginStudent(
      new StudentLoginRequest("STU/1234", "Password1")
    )).isInstanceOf(DisabledException.class)
      .hasMessage("Your registration is awaiting administrator verification.");
  }

  @Test
  void studentLoginRejectsRejectedAccount() {
    when(authenticationManager.authenticate(any()))
      .thenThrow(new DisabledException("Your registration was not approved."));

    assertThatThrownBy(() -> authenticationService.loginStudent(
      new StudentLoginRequest("STU/1234", "Password1")
    )).isInstanceOf(DisabledException.class);
  }

  @Test
  void studentLoginRejectsSuspendedAccount() {
    when(authenticationManager.authenticate(any()))
      .thenThrow(new LockedException("Your account has been suspended."));

    assertThatThrownBy(() -> authenticationService.loginStudent(
      new StudentLoginRequest("STU/1234", "Password1")
    )).isInstanceOf(LockedException.class);
  }

  @Test
  void adminLoginSucceedsForAdministrator() {
    User admin = TestUsers.admin(AccountStatus.ACTIVE, "hash", true);
    AppUserDetails principal = AppUserDetails.from(admin, "admin@example.com");
    stubSuccessfulAuthentication(principal);
    when(jwtService.generateAccessToken(principal)).thenReturn("admin-token");

    LoginResponse response = authenticationService.loginAdmin(
      new AdminLoginRequest("Admin@Example.com", "Password1")
    );

    ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
    verify(authenticationManager).authenticate(captor.capture());
    assertThat(captor.getValue().getPrincipal()).isEqualTo("admin@example.com");
    assertThat(response.accessToken()).isEqualTo("admin-token");
    assertThat(response.user().passwordChangeRequired()).isTrue();
    assertThat(response.user().permissions()).contains("STUDENT_APPROVE");
  }

  @Test
  void adminLoginSucceedsForSuperAdmin() {
    User superAdmin = TestUsers.superAdmin("hash");
    AppUserDetails principal = AppUserDetails.from(superAdmin, "superadmin@example.com");
    stubSuccessfulAuthentication(principal);
    when(jwtService.generateAccessToken(principal)).thenReturn("super-token");

    LoginResponse response = authenticationService.loginAdmin(
      new AdminLoginRequest("superadmin@example.com", "Password1")
    );

    assertThat(response.user().roles()).contains("SUPER_ADMIN");
    assertThat(response.accessToken()).isEqualTo("super-token");
  }

  @Test
  void adminLoginRejectsNonAdministrator() {
    User student = TestUsers.student(AccountStatus.ACTIVE, "hash");
    AppUserDetails principal = AppUserDetails.from(student, "ada@example.com");
    stubSuccessfulAuthentication(principal);

    assertThatThrownBy(() -> authenticationService.loginAdmin(
      new AdminLoginRequest("ada@example.com", "Password1")
    )).isInstanceOf(AccessDeniedException.class)
      .hasMessage("Administrator access is required.");

    verify(jwtService, never()).generateAccessToken(any(UserDetails.class));
  }

  @Test
  void changePasswordUpdatesHashAndClearsFlag() {
    User admin = TestUsers.admin(AccountStatus.ACTIVE, "old-hash", true);
    AppUserDetails principal = AppUserDetails.from(admin, "admin@example.com");
    Authentication authentication = new UsernamePasswordAuthenticationToken(
      principal,
      null,
      principal.getAuthorities()
    );

    when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(passwordService.matches("old-password", "old-hash")).thenReturn(true);
    when(passwordService.encode("NewPassword1")).thenReturn("new-hash");

    authenticationService.changePassword(
      authentication,
      new FirstLoginPasswordChangeRequest("old-password", "NewPassword1", "NewPassword1")
    );

    assertThat(admin.getPasswordHash()).isEqualTo("new-hash");
    assertThat(admin.isPasswordChangeRequired()).isFalse();
    verify(userRepository).save(admin);
  }

  @Test
  void changePasswordRejectsIncorrectCurrentPassword() {
    User admin = TestUsers.admin(AccountStatus.ACTIVE, "old-hash", true);
    AppUserDetails principal = AppUserDetails.from(admin, "admin@example.com");
    Authentication authentication = new UsernamePasswordAuthenticationToken(
      principal,
      null,
      principal.getAuthorities()
    );

    when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(passwordService.matches("wrong", "old-hash")).thenReturn(false);

    assertThatThrownBy(() -> authenticationService.changePassword(
      authentication,
      new FirstLoginPasswordChangeRequest("wrong", "NewPassword1", "NewPassword1")
    )).isInstanceOf(BadCredentialsException.class)
      .hasMessage("Current password is incorrect.");

    verify(userRepository, never()).save(any());
  }

  @Test
  void changePasswordRejectsMismatchedConfirmation() {
    User admin = TestUsers.admin(AccountStatus.ACTIVE, "old-hash", true);
    AppUserDetails principal = AppUserDetails.from(admin, "admin@example.com");
    Authentication authentication = new UsernamePasswordAuthenticationToken(
      principal,
      null,
      principal.getAuthorities()
    );

    when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(passwordService.matches("old-password", "old-hash")).thenReturn(true);

    assertThatThrownBy(() -> authenticationService.changePassword(
      authentication,
      new FirstLoginPasswordChangeRequest("old-password", "NewPassword1", "OtherPassword1")
    )).isInstanceOf(BusinessRuleException.class)
      .hasMessage("New password and confirmation do not match.");
  }

  @Test
  void changePasswordUsesAuthenticatedPrincipalOnly() {
    User admin = TestUsers.admin(AccountStatus.ACTIVE, "old-hash", true);
    AppUserDetails principal = AppUserDetails.from(admin, "admin@example.com");
    Authentication authentication = new UsernamePasswordAuthenticationToken(
      principal,
      null,
      principal.getAuthorities()
    );

    when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(passwordService.matches("old-password", "old-hash")).thenReturn(true);
    when(passwordService.encode("NewPassword1")).thenReturn("new-hash");

    authenticationService.changePassword(
      authentication,
      new FirstLoginPasswordChangeRequest("old-password", "NewPassword1", "NewPassword1")
    );

    verify(userRepository).findById(admin.getId());
    verify(userRepository, never()).findByEmail(any());
  }

  private void stubSuccessfulAuthentication(AppUserDetails principal) {
    Authentication authentication = new UsernamePasswordAuthenticationToken(
      principal,
      null,
      principal.getAuthorities()
    );
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
  }
}
