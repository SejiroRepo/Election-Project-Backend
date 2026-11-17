package com.ellectronic.voting.admin.controller;

import com.ellectronic.voting.auth.dto.AdminCreationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class AdminControllerAuthorizationTest {

  @Test
  void createEndpointRequiresSuperAdminRole() throws Exception {
    Method method = AdminController.class.getMethod("create", AdminCreationRequest.class);
    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

    assertThat(preAuthorize).isNotNull();
    assertThat(preAuthorize.value()).isEqualTo("hasRole('SUPER_ADMIN')");
  }
}
