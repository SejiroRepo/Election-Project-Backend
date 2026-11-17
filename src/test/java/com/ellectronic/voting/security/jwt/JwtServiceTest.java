package com.ellectronic.voting.security.jwt;

import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.security.user.AppUserDetails;
import com.ellectronic.voting.testsupport.TestUsers;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

  private final JwtService jwtService = new JwtService(
    Base64.getEncoder().encodeToString("unit-test-jwt-secret-key-32bytes!".getBytes()),
    3_600_000L
  );

  @Test
  void generatesValidAccessTokenForStudentAndAdmin() {
    UserDetails student = AppUserDetails.from(
      TestUsers.student(AccountStatus.ACTIVE, "hash"),
      "STU/1234"
    );
    UserDetails admin = AppUserDetails.from(
      TestUsers.admin(AccountStatus.ACTIVE, "hash", false),
      "admin@example.com"
    );

    String studentToken = jwtService.generateAccessToken(student);
    String adminToken = jwtService.generateAccessToken(admin);

    assertThat(jwtService.extractUsername(studentToken)).isEqualTo("STU/1234");
    assertThat(jwtService.extractUsername(adminToken)).isEqualTo("admin@example.com");
    assertThat(jwtService.isTokenValid(studentToken, student)).isTrue();
    assertThat(jwtService.isTokenValid(adminToken, admin)).isTrue();
  }

  @Test
  void rejectsTokenWhenAccountIsDisabled() {
    UserDetails active = AppUserDetails.from(
      TestUsers.student(AccountStatus.ACTIVE, "hash"),
      "STU/1234"
    );
    UserDetails suspended = AppUserDetails.from(
      TestUsers.student(AccountStatus.SUSPENDED, "hash"),
      "STU/1234"
    );

    String token = jwtService.generateAccessToken(active);

    assertThat(jwtService.isTokenValid(token, suspended)).isFalse();
  }
}
