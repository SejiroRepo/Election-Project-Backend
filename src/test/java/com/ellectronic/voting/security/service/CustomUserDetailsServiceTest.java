package com.ellectronic.voting.security.service;

import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.enums.PermissionName;
import com.ellectronic.voting.security.entity.Permission;
import com.ellectronic.voting.security.user.AppUserDetails;
import com.ellectronic.voting.testsupport.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  private CustomUserDetailsService userDetailsService;

  @BeforeEach
  void setUp() {
    userDetailsService = new CustomUserDetailsService(userRepository);
  }

  @Test
  void loadsStudentByMatricNumber() {
    User student = TestUsers.student(AccountStatus.ACTIVE, "hash");
    when(userRepository.findByMatricNumber("STU/1234")).thenReturn(Optional.of(student));

    AppUserDetails details = (AppUserDetails) userDetailsService.loadUserByUsername("stu/1234");

    assertThat(details.getUsername()).isEqualTo("STU/1234");
    assertThat(details.getAuthorities())
      .extracting(GrantedAuthority::getAuthority)
      .contains("ROLE_STUDENT", "STUDENT_VIEW");
    assertThat(details.isEnabled()).isTrue();
  }

  @Test
  void loadsAdministratorByEmail() {
    User admin = TestUsers.admin(AccountStatus.ACTIVE, "hash", false);
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

    AppUserDetails details = (AppUserDetails) userDetailsService.loadUserByUsername("Admin@Example.com");

    assertThat(details.getUsername()).isEqualTo("admin@example.com");
    assertThat(details.getAuthorities())
      .extracting(GrantedAuthority::getAuthority)
      .contains("ROLE_ADMIN", "STUDENT_APPROVE");
  }

  @Test
  void unionsRoleAndUserSpecificPermissionsWithoutDuplicates() {
    User admin = TestUsers.admin(AccountStatus.ACTIVE, "hash", false);
    admin.addPermission(new Permission(PermissionName.STUDENT_APPROVE.name(), "already inherited"));
    admin.addPermission(new Permission(PermissionName.REPORTS_VIEW.name(), "user specific"));
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

    AppUserDetails details = (AppUserDetails) userDetailsService.loadUserByUsername("admin@example.com");

    Set<String> authorities = details.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toSet());

    assertThat(authorities).contains("STUDENT_APPROVE", "REPORTS_VIEW", "ROLE_ADMIN");
    assertThat(authorities.stream().filter("STUDENT_APPROVE"::equals).count()).isEqualTo(1);
  }

  @Test
  void disablesNonActiveAccounts() {
    User pending = TestUsers.student(AccountStatus.PENDING, "hash");
    when(userRepository.findByMatricNumber("STU/1234")).thenReturn(Optional.of(pending));

    AppUserDetails details = (AppUserDetails) userDetailsService.loadUserByUsername("STU/1234");

    assertThat(details.isEnabled()).isFalse();
    assertThat(details.getAccountStatus()).isEqualTo(AccountStatus.PENDING);
  }

  @Test
  void throwsWhenIdentifierIsUnknown() {
    when(userRepository.findByMatricNumber("MISSING")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("MISSING"))
      .isInstanceOf(UsernameNotFoundException.class);
  }
}
