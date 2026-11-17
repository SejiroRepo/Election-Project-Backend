package com.ellectronic.voting.security.user;

import com.ellectronic.voting.common.enums.AccountStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

public class AccountStatusUserDetailsChecker implements UserDetailsChecker {

  @Override
  public void check(UserDetails user) {
    if (user instanceof AppUserDetails appUser) {
      AccountStatus status = appUser.getAccountStatus();

      if (status == AccountStatus.PENDING) {
        throw new DisabledException("Your registration is awaiting administrator verification.");
      }
      if (status == AccountStatus.REJECTED) {
        throw new DisabledException("Your registration was not approved.");
      }
      if (status == AccountStatus.SUSPENDED) {
        throw new LockedException("Your account has been suspended.");
      }
    }

    if (!user.isAccountNonLocked()) {
      throw new LockedException("Your account has been suspended.");
    }
    if (!user.isEnabled()) {
      throw new DisabledException("This account is not active.");
    }
    if (!user.isAccountNonExpired()) {
      throw new AccountExpiredException("This account is not active.");
    }
    if (!user.isCredentialsNonExpired()) {
      throw new CredentialsExpiredException("Your credentials have expired.");
    }
  }
}