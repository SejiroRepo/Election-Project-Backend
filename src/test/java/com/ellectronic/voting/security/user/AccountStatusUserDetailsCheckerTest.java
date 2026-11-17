package com.ellectronic.voting.security.user;

import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.testsupport.TestUsers;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountStatusUserDetailsCheckerTest {

  private final AccountStatusUserDetailsChecker checker = new AccountStatusUserDetailsChecker();

  @Test
  void allowsActiveAccounts() {
    AppUserDetails details = AppUserDetails.from(
      TestUsers.student(AccountStatus.ACTIVE, "hash"),
      "STU/1234"
    );

    assertThatCode(() -> checker.check(details)).doesNotThrowAnyException();
  }

  @Test
  void rejectsPendingAccounts() {
    AppUserDetails details = AppUserDetails.from(
      TestUsers.student(AccountStatus.PENDING, "hash"),
      "STU/1234"
    );

    assertThatThrownBy(() -> checker.check(details))
      .isInstanceOf(DisabledException.class)
      .hasMessage("Your registration is awaiting administrator verification.");
  }

  @Test
  void rejectsRejectedAccounts() {
    AppUserDetails details = AppUserDetails.from(
      TestUsers.student(AccountStatus.REJECTED, "hash"),
      "STU/1234"
    );

    assertThatThrownBy(() -> checker.check(details))
      .isInstanceOf(DisabledException.class)
      .hasMessage("Your registration was not approved.");
  }

  @Test
  void rejectsSuspendedAccounts() {
    AppUserDetails details = AppUserDetails.from(
      TestUsers.student(AccountStatus.SUSPENDED, "hash"),
      "STU/1234"
    );

    assertThatThrownBy(() -> checker.check(details))
      .isInstanceOf(LockedException.class)
      .hasMessage("Your account has been suspended.");
  }
}
