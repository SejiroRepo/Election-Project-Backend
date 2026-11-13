package com.ellectronic.voting.security.initializer;

import com.ellectronic.voting.common.enums.PermissionName;
import com.ellectronic.voting.common.enums.RoleName;
import com.ellectronic.voting.security.entity.Permission;
import com.ellectronic.voting.security.entity.Role;
import com.ellectronic.voting.security.repository.RoleRepository;
import com.ellectronic.voting.security.repository.PermissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component 
public class SecurityDataInitializer implements CommandLineRunner{

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  public SecurityDataInitializer(RoleRepository roleRepository, PermissionRepository permissionRepository) {
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
      initializePermissions();
      initializeRoles();
  }

  private void initializePermissions() {
    for (PermissionName permissionName : PermissionName.values()) {
      permissionRepository.findByName(permissionName.name())
        .orElseGet(() -> permissionRepository.save(
          new Permission(
            permissionName.name(),
            permissionName.name()
              .toLowerCase()
              .replace('_', ' ')
          )
        ));
    }
  }

  private void initializeRoles() {
    Map<RoleName, Set<PermissionName>> rolePermissions = getDefaultRolePermissions();

    for (Map.Entry<RoleName, Set<PermissionName>> entry : rolePermissions.entrySet()) {

      RoleName roleName = entry.getKey();

      Role role = roleRepository.findByName(roleName) .orElseGet(() -> roleRepository.save(
        new Role(roleName, roleName.name()
          .toLowerCase()
          .replace('_', ' ')
        )
      ));

      for (PermissionName permissionName : entry.getValue()) {
        Permission permission = permissionRepository.findByName(permissionName.name()).orElseThrow(() -> new IllegalStateException(
          "Permission not initialized: "
          + permissionName.name()
        ));

        role.addPermission(permission);
      }

      roleRepository.save(role);
    }
  }

  private Map<RoleName, Set<PermissionName>> getDefaultRolePermissions() {

    Map<RoleName, Set<PermissionName>> permissions = new EnumMap<>(RoleName.class);

      permissions.put(
        RoleName.STUDENT,
        EnumSet.of(
          PermissionName.STUDENT_VIEW,
          PermissionName.ELECTION_VIEW,
          PermissionName.STUDENT_CAST_VOTE,
          PermissionName.VOTE_CONFIRMATION_VIEW,
          PermissionName.ELECTION_RESULTS_VIEW,
          PermissionName.CONTESTANT_APPLY
        )
      );

      permissions.put(
        RoleName.CONTESTANT,
        EnumSet.of(
          PermissionName.CONTESTANT_VIEW,
          PermissionName.ELECTION_VIEW,
          PermissionName.ELECTION_RESULTS_VIEW,
          PermissionName.DASHBOARD_VIEW
        )
      );

      permissions.put(
        RoleName.ADMIN,
        EnumSet.of(
          PermissionName.STUDENT_VIEW,
          PermissionName.STUDENT_APPROVE,
          PermissionName.STUDENT_REJECT,
          PermissionName.STUDENT_MANAGE,

          PermissionName.CONTESTANT_VIEW,
          PermissionName.CONTESTANT_APPROVE,
          PermissionName.CONTESTANT_REJECT,
          PermissionName.CONTESTANT_MANAGE,

          PermissionName.ELECTION_VIEW,
          PermissionName.ELECTION_CREATE,
          PermissionName.ELECTION_UPDATE,
          PermissionName.ELECTION_DELETE,
          PermissionName.ELECTION_MANAGE,

          PermissionName.DASHBOARD_VIEW,
          PermissionName.REPORTS_VIEW,
          PermissionName.NOTIFICATION_MANAGE
        )
      );

      permissions.put(
        RoleName.SUPER_ADMIN,
        EnumSet.allOf(PermissionName.class)
      );

    return permissions;
  }
}