package com.ellectronic.voting.auth.service;

import com.ellectronic.voting.auth.dto.AdminCreationRequest;
import com.ellectronic.voting.auth.dto.AdminCreationResponse;
import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.auth.support.TemporaryPasswordGenerator;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.enums.PermissionName;
import com.ellectronic.voting.common.enums.RoleName;
import com.ellectronic.voting.common.exception.BusinessRuleException;
import com.ellectronic.voting.common.exception.DuplicateResourceException;
import com.ellectronic.voting.security.entity.Permission;
import com.ellectronic.voting.security.entity.Role;
import com.ellectronic.voting.security.repository.PermissionRepository;
import com.ellectronic.voting.security.repository.RoleRepository;
import com.ellectronic.voting.security.service.PasswordService;
import com.ellectronic.voting.testsupport.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProvisioningServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PermissionRepository permissionRepository;

  @Mock
  private PasswordService passwordService;

  @Mock
  private TemporaryPasswordGenerator temporaryPasswordGenerator;

  private AdminProvisioningService adminProvisioningService;

  @BeforeEach
  void setUp() {
    adminProvisioningService = new AdminProvisioningService(
      userRepository,
      roleRepository,
      permissionRepository,
      passwordService,
      temporaryPasswordGenerator
    );
  }

  @Test
  void superAdminCreatesAdministratorWithGeneratedPasswordAndUserPermissions() {
    Role adminRole = TestUsers.roleWithPermission(RoleName.ADMIN, PermissionName.STUDENT_VIEW);
    Permission reportsView = new Permission(PermissionName.REPORTS_VIEW.name(), "reports");

    when(userRepository.existsByEmail("new.admin@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
    when(permissionRepository.findByName(PermissionName.REPORTS_VIEW.name())).thenReturn(Optional.of(reportsView));
    when(temporaryPasswordGenerator.generateEightDigitPassword()).thenReturn("12345678");
    when(passwordService.encode("12345678")).thenReturn("$2a$10$hashed");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      TestUsers.assignId(user, UUID.fromString("44444444-4444-4444-4444-444444444444"));
      return user;
    });

    AdminCreationResponse response = adminProvisioningService.createAdministrator(
      new AdminCreationRequest(
        "Ngozi",
        "Eze",
        "New.Admin@Example.com",
        EnumSet.of(RoleName.ADMIN),
        EnumSet.of(PermissionName.REPORTS_VIEW)
      )
    );

    assertThat(response.temporaryPassword()).isEqualTo("12345678");
    assertThat(response.temporaryPassword()).matches("\\d{8}");
    assertThat(response.passwordChangeRequired()).isTrue();
    assertThat(response.accountStatus()).isEqualTo(AccountStatus.ACTIVE);
    assertThat(response.roles()).containsExactly("ADMIN");
    assertThat(response.permissions()).containsExactly("REPORTS_VIEW");
    assertThat(response.email()).isEqualTo("new.admin@example.com");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$hashed");
    assertThat(saved.getPasswordHash()).isNotEqualTo("12345678");
    assertThat(saved.getMatricNumber()).isNull();
  }

  @Test
  void generatedPasswordIsBCryptHashedUsingPasswordService() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    Role adminRole = TestUsers.roleWithPermission(RoleName.ADMIN, PermissionName.STUDENT_VIEW);

    when(userRepository.existsByEmail("bcrypt.admin@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
    when(temporaryPasswordGenerator.generateEightDigitPassword()).thenReturn("87654321");
    when(passwordService.encode("87654321")).thenReturn(encoder.encode("87654321"));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    adminProvisioningService.createAdministrator(
      new AdminCreationRequest("B", "Crypt", "bcrypt.admin@example.com", null, null)
    );

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getPasswordHash()).startsWith("$2a$");
    assertThat(encoder.matches("87654321", captor.getValue().getPasswordHash())).isTrue();
  }

  @Test
  void duplicateEmailIsRejected() {
    when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

    assertThatThrownBy(() -> adminProvisioningService.createAdministrator(
      new AdminCreationRequest("Ada", "Admin", "admin@example.com", null, null)
    )).isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void superAdminRoleCannotBeAssignedThroughOrdinaryCreation() {
    when(userRepository.existsByEmail("elevated@example.com")).thenReturn(false);

    assertThatThrownBy(() -> adminProvisioningService.createAdministrator(
      new AdminCreationRequest(
        "Elevated",
        "Admin",
        "elevated@example.com",
        EnumSet.of(RoleName.SUPER_ADMIN),
        null
      )
    )).isInstanceOf(BusinessRuleException.class)
      .hasMessage("SUPER_ADMIN cannot be assigned through administrator creation.");
  }

  @Test
  void studentRoleCannotBeAssignedToAdministrator() {
    when(userRepository.existsByEmail("invalid-role@example.com")).thenReturn(false);

    assertThatThrownBy(() -> adminProvisioningService.createAdministrator(
      new AdminCreationRequest(
        "Invalid",
        "Role",
        "invalid-role@example.com",
        EnumSet.of(RoleName.STUDENT),
        null
      )
    )).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void unknownPermissionIsRejected() {
    Role adminRole = TestUsers.roleWithPermission(RoleName.ADMIN, PermissionName.STUDENT_VIEW);
    when(userRepository.existsByEmail("perm@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
    when(permissionRepository.findByName(PermissionName.SYSTEM_MANAGE.name())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminProvisioningService.createAdministrator(
      new AdminCreationRequest(
        "Perm",
        "Admin",
        "perm@example.com",
        EnumSet.of(RoleName.ADMIN),
        EnumSet.of(PermissionName.SYSTEM_MANAGE)
      )
    )).isInstanceOf(BusinessRuleException.class)
      .hasMessageContaining("Permission is not available");
  }
}
