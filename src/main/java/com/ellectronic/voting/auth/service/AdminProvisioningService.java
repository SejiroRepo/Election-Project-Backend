package com.ellectronic.voting.auth.service;

import com.ellectronic.voting.auth.dto.AdminCreationRequest;
import com.ellectronic.voting.auth.dto.AdminCreationResponse;
import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.auth.support.IdentifierNormalizer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminProvisioningService {

  private static final Logger log = LoggerFactory.getLogger(AdminProvisioningService.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final PasswordService passwordService;
  private final TemporaryPasswordGenerator temporaryPasswordGenerator;

  public AdminProvisioningService(
    UserRepository userRepository,
    RoleRepository roleRepository,
    PermissionRepository permissionRepository,
    PasswordService passwordService,
    TemporaryPasswordGenerator temporaryPasswordGenerator
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.passwordService = passwordService;
    this.temporaryPasswordGenerator = temporaryPasswordGenerator;
  }

  @Transactional
  public AdminCreationResponse createAdministrator(AdminCreationRequest request) {
    String email = IdentifierNormalizer.normalizeEmail(request.email());

    if (userRepository.existsByEmail(email)) {
      throw new DuplicateResourceException("An account with this email already exists.");
    }

    Set<RoleName> requestedRoles = resolveRoles(request.roles());
    Set<PermissionName> requestedPermissions = resolvePermissions(request.permissions());

    String temporaryPassword = temporaryPasswordGenerator.generateEightDigitPassword();

    User administrator = new User(
      request.firstName().trim(),
      request.lastName().trim(),
      null,
      email,
      passwordService.encode(temporaryPassword)
    );
    administrator.setAccountStatus(AccountStatus.ACTIVE);
    administrator.setPasswordChangeRequired(true);

    for (RoleName roleName : requestedRoles) {
      Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new BusinessRuleException("Role is not available: " + roleName));
      administrator.addRole(role);
    }

    for (PermissionName permissionName : requestedPermissions) {
      Permission permission = permissionRepository.findByName(permissionName.name())
        .orElseThrow(() -> new BusinessRuleException("Permission is not available: " + permissionName));
      administrator.addPermission(permission);
    }

    User saved = userRepository.save(administrator);

    log.info("Created administrator account {}", saved.getId());

    return new AdminCreationResponse(
      saved.getId(),
      saved.getFirstName(),
      saved.getLastName(),
      saved.getEmail(),
      saved.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toCollection(LinkedHashSet::new)),
      saved.getPermissions().stream().map(Permission::getName).collect(Collectors.toCollection(LinkedHashSet::new)),
      saved.getAccountStatus(),
      temporaryPassword,
      saved.isPasswordChangeRequired()
    );
  }

  private Set<RoleName> resolveRoles(Set<RoleName> requestedRoles) {
    Set<RoleName> roles = (requestedRoles == null || requestedRoles.isEmpty())
      ? EnumSet.of(RoleName.ADMIN)
      : EnumSet.copyOf(requestedRoles);

    for (RoleName role : roles) {
      if (role == RoleName.SUPER_ADMIN) {
        throw new BusinessRuleException("SUPER_ADMIN cannot be assigned through administrator creation.");
      }
      if (role != RoleName.ADMIN) {
        throw new BusinessRuleException("Only the ADMIN role can be assigned when creating an administrator.");
      }
    }

    return roles;
  }

  private Set<PermissionName> resolvePermissions(Set<PermissionName> requestedPermissions) {
    if (requestedPermissions == null || requestedPermissions.isEmpty()) {
      return EnumSet.noneOf(PermissionName.class);
    }
    return EnumSet.copyOf(requestedPermissions);
  }
}
