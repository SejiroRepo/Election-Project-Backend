package com.ellectronic.voting.security.repository;

import com.ellectronic.voting.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;


public interface PermissionRepository extends JpaRepository<Permission, UUID> {
  
  Optional<Permission> findByName(String name);
  boolean existByName(String name);
}
