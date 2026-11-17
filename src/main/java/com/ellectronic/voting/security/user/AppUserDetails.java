package com.ellectronic.voting.security.user;

import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.enums.RoleName;
import com.ellectronic.voting.security.entity.Permission;
import com.ellectronic.voting.security.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AppUserDetails implements UserDetails {

  private static final long serialVersionUID = 1L;

  private final UUID id;
  private final String username;
  private final String password;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String matricNumber;
  private final AccountStatus accountStatus;
  private final boolean passwordChangeRequired;
  private final Set<String> roles;
  private final Set<String> permissions;
  private final Set<GrantedAuthority> authorities;

  private AppUserDetails(
    UUID id,
    String username,
    String password,
    String firstName,
    String lastName,
    String email,
    String matricNumber,
    AccountStatus accountStatus,
    boolean passwordChangeRequired,
    Set<String> roles,
    Set<String> permissions,
    Set<GrantedAuthority> authorities
  ) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.matricNumber = matricNumber;
    this.accountStatus = accountStatus;
    this.passwordChangeRequired = passwordChangeRequired;
    this.roles = Collections.unmodifiableSet(roles);
    this.permissions = Collections.unmodifiableSet(permissions);
    this.authorities = Collections.unmodifiableSet(authorities);
  }

  public static AppUserDetails from(User user, String username) {
    Set<String> roleNames = new HashSet<>();
    Set<String> permissionNames = new HashSet<>();
    Set<GrantedAuthority> authorities = new HashSet<>();

    for (Role role : user.getRoles()) {
      String roleName = role.getName().name();
      roleNames.add(roleName);
      authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

      for (Permission permission : role.getPermissions()) {
        permissionNames.add(permission.getName());
        authorities.add(new SimpleGrantedAuthority(permission.getName()));
      }
    }

    if (user.getPermissions() != null) {
      for (Permission permission : user.getPermissions()) {
        permissionNames.add(permission.getName());
        authorities.add(new SimpleGrantedAuthority(permission.getName()));
      }
    }

    return new AppUserDetails(
      user.getId(),
      username,
      user.getPasswordHash(),
      user.getFirstName(),
      user.getLastName(),
      user.getEmail(),
      user.getMatricNumber(),
      user.getAccountStatus(),
      user.isPasswordChangeRequired(),
      roleNames,
      permissionNames,
      authorities
    );
  }

  public UUID getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getMatricNumber() {
    return matricNumber;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public boolean isPasswordChangeRequired() {
    return passwordChangeRequired;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public Set<String> getPermissions() {
    return permissions;
  }

  public boolean hasAnyRole(RoleName... roleNames) {
    EnumSet<RoleName> expected = EnumSet.noneOf(RoleName.class);
    Collections.addAll(expected, roleNames);
    return expected.stream().map(RoleName::name).anyMatch(roles::contains);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return accountStatus != AccountStatus.SUSPENDED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return accountStatus == AccountStatus.ACTIVE;
  }
}