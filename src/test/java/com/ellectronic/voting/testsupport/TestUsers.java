package com.ellectronic.voting.testsupport;

import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.enums.PermissionName;
import com.ellectronic.voting.common.enums.RoleName;
import com.ellectronic.voting.security.entity.Permission;
import com.ellectronic.voting.security.entity.Role;

import java.lang.reflect.Field;
import java.util.UUID;

public final class TestUsers {

  private TestUsers() {
  }

  public static User student(AccountStatus status, String passwordHash) {
    User user = new User("Ada", "Okeke", "STU/1234", "ada@example.com", passwordHash);
    user.setAccountStatus(status);
    user.addRole(roleWithPermission(RoleName.STUDENT, PermissionName.STUDENT_VIEW));
    assignId(user, UUID.fromString("11111111-1111-1111-1111-111111111111"));
    return user;
  }

  public static User admin(AccountStatus status, String passwordHash, boolean passwordChangeRequired) {
    User user = new User("Ada", "Admin", null, "admin@example.com", passwordHash);
    user.setAccountStatus(status);
    user.setPasswordChangeRequired(passwordChangeRequired);
    user.addRole(roleWithPermission(RoleName.ADMIN, PermissionName.STUDENT_APPROVE));
    assignId(user, UUID.fromString("22222222-2222-2222-2222-222222222222"));
    return user;
  }

  public static User superAdmin(String passwordHash) {
    User user = new User("Super", "Admin", null, "superadmin@example.com", passwordHash);
    user.setAccountStatus(AccountStatus.ACTIVE);
    user.addRole(roleWithPermission(RoleName.SUPER_ADMIN, PermissionName.ADMIN_CREATE));
    assignId(user, UUID.fromString("33333333-3333-3333-3333-333333333333"));
    return user;
  }

  public static Role roleWithPermission(RoleName roleName, PermissionName permissionName) {
    Role role = new Role(roleName, roleName.name());
    role.addPermission(new Permission(permissionName.name(), permissionName.name()));
    return role;
  }

  public static void assignId(User user, UUID id) {
    try {
      Field field = User.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(user, id);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
