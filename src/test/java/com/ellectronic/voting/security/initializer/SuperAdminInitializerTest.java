package com.ellectronic.voting.security.initializer;

import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.enums.PermissionName;
import com.ellectronic.voting.common.enums.RoleName;
import com.ellectronic.voting.security.entity.Role;
import com.ellectronic.voting.security.repository.RoleRepository;
import com.ellectronic.voting.security.service.PasswordService;
import com.ellectronic.voting.testsupport.TestUsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminInitializerTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordService passwordService;

  @Test
  void createsInitialSuperAdminWithRoleAndHashedPassword() {
    Role superAdminRole = TestUsers.roleWithPermission(RoleName.SUPER_ADMIN, PermissionName.ADMIN_CREATE);
    when(userRepository.existsByEmail("superadmin@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.SUPER_ADMIN)).thenReturn(Optional.of(superAdminRole));
    when(passwordService.encode("SuperPass12")).thenReturn("hashed-super-pass");

    SuperAdminInitializer initializer = new SuperAdminInitializer(
      userRepository,
      roleRepository,
      passwordService,
      "SuperAdmin@Example.com",
      "SuperPass12"
    );

    initializer.run();

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();

    assertThat(saved.getEmail()).isEqualTo("superadmin@example.com");
    assertThat(saved.getPasswordHash()).isEqualTo("hashed-super-pass");
    assertThat(saved.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    assertThat(saved.isPasswordChangeRequired()).isFalse();
    assertThat(saved.getRoles()).extracting(role -> role.getName()).containsExactly(RoleName.SUPER_ADMIN);
    assertThat(saved.getRoles().iterator().next().getPermissions())
      .extracting(permission -> permission.getName())
      .contains(PermissionName.ADMIN_CREATE.name());
  }

  @Test
  void doesNotCreateDuplicateOrOverwriteExistingSuperAdmin() {
    when(userRepository.existsByEmail("superadmin@example.com")).thenReturn(true);

    SuperAdminInitializer initializer = new SuperAdminInitializer(
      userRepository,
      roleRepository,
      passwordService,
      "superadmin@example.com",
      "ChangedPassword1"
    );

    initializer.run();

    verify(userRepository, never()).save(any());
    verify(passwordService, never()).encode(any());
  }

  @Test
  void failsWhenBootstrapCredentialsAreMissing() {
    SuperAdminInitializer initializer = new SuperAdminInitializer(
      userRepository,
      roleRepository,
      passwordService,
      " ",
      null
    );

    assertThatThrownBy(initializer::run)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ADMIN_EMAIL")
      .hasMessageContaining("ADMIN_PASSWORD");
  }

  @Test
  void failsWhenEmailIsInvalid() {
    SuperAdminInitializer initializer = new SuperAdminInitializer(
      userRepository,
      roleRepository,
      passwordService,
      "not-an-email",
      "SuperPass12"
    );

    assertThatThrownBy(initializer::run)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ADMIN_EMAIL");
  }
}
