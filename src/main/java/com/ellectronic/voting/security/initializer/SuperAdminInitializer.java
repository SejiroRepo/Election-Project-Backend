package com.ellectronic.voting.security.initializer;

import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.auth.support.IdentifierNormalizer;
import com.ellectronic.voting.auth.support.PasswordPolicy;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.enums.RoleName;
import com.ellectronic.voting.security.entity.Role;
import com.ellectronic.voting.security.repository.RoleRepository;
import com.ellectronic.voting.security.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class SuperAdminInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(SuperAdminInitializer.class);
  private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordService passwordService;
  private final String configuredEmail;
  private final String configuredPassword;

  public SuperAdminInitializer(
    UserRepository userRepository,
    RoleRepository roleRepository,
    PasswordService passwordService,
    @Value("${app.admin.email:}") String configuredEmail,
    @Value("${app.admin.password:}") String configuredPassword
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordService = passwordService;
    this.configuredEmail = configuredEmail;
    this.configuredPassword = configuredPassword;
  }

  @Override
  @Transactional
  public void run(String... args) {
    String email = IdentifierNormalizer.normalizeEmail(configuredEmail);
    validateBootstrapCredentials(email, configuredPassword);

    if (userRepository.existsByEmail(email)) {
      log.info("Super Admin account already exists; bootstrap skipped.");
      return;
    }

    Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN)
      .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role has not been initialized."));

    User superAdmin = new User(
      "Super",
      "Admin",
      null,
      email,
      passwordService.encode(configuredPassword)
    );
    superAdmin.setAccountStatus(AccountStatus.ACTIVE);
    superAdmin.setPasswordChangeRequired(false);
    superAdmin.addRole(superAdminRole);

    userRepository.save(superAdmin);
    log.info("Super Admin account created.");
  }

  private void validateBootstrapCredentials(String email, String password) {
    if (email == null || email.isBlank() || password == null || password.isBlank()) {
      throw new IllegalStateException(
        "Super Admin bootstrap requires ADMIN_EMAIL and ADMIN_PASSWORD to be configured."
      );
    }

    if (!email.matches(EMAIL_PATTERN)) {
      throw new IllegalStateException("ADMIN_EMAIL must be a valid email address.");
    }

    try {
      PasswordPolicy.validate(password);
    } catch (RuntimeException exception) {
      throw new IllegalStateException("ADMIN_PASSWORD is invalid: " + exception.getMessage());
    }
  }
}