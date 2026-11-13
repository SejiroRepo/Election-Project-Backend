package com.ellectronic.voting.auth.repository;

import com.ellectronic.voting.auth.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByMatricNumber(String matricNumber);

    Optional<User> findByEmail(String email);

    boolean existsByMatricNumber(String matricNumber);

    boolean existsByEmail(String email);
}