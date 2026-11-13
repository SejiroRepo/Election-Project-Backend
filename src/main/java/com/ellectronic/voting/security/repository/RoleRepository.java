package com.ellectronic.voting.security.repository;

import com.ellectronic.voting.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ellectronic.voting.common.enums.RoleName;


import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID>{
  
  Optional<Role> findByName(RoleName name);
  boolean existsByName(RoleName name);
}