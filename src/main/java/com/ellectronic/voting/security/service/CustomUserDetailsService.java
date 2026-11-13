package com.ellectronic.voting.security.service;

import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.security.entity.Permission;
import com.ellectronic.voting.security.entity.Role;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

  @Override
  public UserDetails loadUserByUsername(String matricNumber) throws UsernameNotFoundException {

    User user = userRepository.findByMatricNumber(matricNumber)
      .orElseThrow(() -> new UsernameNotFoundException(
              "User not found with matric number: " + matricNumber
      ));

    Set<SimpleGrantedAuthority> authorities = new HashSet<>();

    for (Role role : user.getRoles()) {

      // Add role authority.
      authorities.add(
        new SimpleGrantedAuthority(
                "ROLE_" + role.getName().name()
        )
      );

      // Add permissions assigned to the role.
      for (Permission permission : role.getPermissions()) {
        authorities.add(
                new SimpleGrantedAuthority(permission.getName())
        );
      }
    }

    boolean accountEnabled = user.getAccountStatus() == AccountStatus.ACTIVE;

    return org.springframework.security.core.userdetails.User
      .withUsername(user.getMatricNumber())
      .password(user.getPasswordHash())
      .authorities(authorities)
      .disabled(!accountEnabled)
      .build();
  }
}