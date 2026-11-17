package com.ellectronic.voting.auth.service;

import com.ellectronic.voting.auth.dto.AdminLoginRequest;
import com.ellectronic.voting.auth.dto.AuthenticatedUserResponse;
import com.ellectronic.voting.auth.dto.FirstLoginPasswordChangeRequest;
import com.ellectronic.voting.auth.dto.LoginResponse;
import com.ellectronic.voting.auth.dto.StudentLoginRequest;
import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.auth.support.IdentifierNormalizer;
import com.ellectronic.voting.auth.support.PasswordPolicy;
import com.ellectronic.voting.common.enums.RoleName;
import com.ellectronic.voting.common.exception.BusinessRuleException;
import com.ellectronic.voting.common.exception.ResourceNotFoundException;
import com.ellectronic.voting.security.jwt.JwtService;
import com.ellectronic.voting.security.service.PasswordService;
import com.ellectronic.voting.security.user.AppUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final PasswordService passwordService;
  private final UserRepository userRepository;

  public AuthenticationService(
    AuthenticationManager authenticationManager,
    JwtService jwtService,
    PasswordService passwordService,
    UserRepository userRepository
  ) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.passwordService = passwordService;
    this.userRepository = userRepository;
  }

  public LoginResponse loginStudent(StudentLoginRequest request) {
    String matricNumber = IdentifierNormalizer.normalizeMatricNumber(request.matricNumber());
    AppUserDetails principal = authenticate(matricNumber, request.password());
    return issueToken(principal);
  }

  public LoginResponse loginAdmin(AdminLoginRequest request) {
    String email = IdentifierNormalizer.normalizeEmail(request.email());
    AppUserDetails principal = authenticate(email, request.password());

    if (!principal.hasAnyRole(RoleName.ADMIN, RoleName.SUPER_ADMIN)) {
      throw new AccessDeniedException("Administrator access is required.");
    }

    return issueToken(principal);
  }

  @Transactional
  public void changePassword(Authentication authentication, FirstLoginPasswordChangeRequest request) {
    AppUserDetails principal = currentUser(authentication);

    User user = userRepository.findById(principal.getId())
      .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found."));

    if (!passwordService.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new BadCredentialsException("Current password is incorrect.");
    }

    if (!request.newPassword().equals(request.confirmPassword())) {
      throw new BusinessRuleException("New password and confirmation do not match.");
    }

    PasswordPolicy.validate(request.newPassword());

    user.setPasswordHash(passwordService.encode(request.newPassword()));
    user.setPasswordChangeRequired(false);
    userRepository.save(user);
  }

  private AppUserDetails authenticate(String principal, String password) {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(principal, password)
    );

    if (!(authentication.getPrincipal() instanceof AppUserDetails appUserDetails)) {
      throw new InsufficientAuthenticationException("Authentication is required.");
    }

    return appUserDetails;
  }

  private LoginResponse issueToken(AppUserDetails principal) {
    String accessToken = jwtService.generateAccessToken(principal);
    return LoginResponse.bearer(accessToken, AuthenticatedUserResponse.from(principal));
  }

  private AppUserDetails currentUser(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
      throw new InsufficientAuthenticationException("Authentication is required.");
    }
    return principal;
  }
}
