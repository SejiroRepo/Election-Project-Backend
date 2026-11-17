package com.ellectronic.voting.security.service;

import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.auth.support.IdentifierNormalizer;
import com.ellectronic.voting.security.user.AppUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    if (identifier == null || identifier.isBlank()) {
      throw new UsernameNotFoundException("User not found");
    }

    String trimmed = identifier.trim();

    if (IdentifierNormalizer.looksLikeEmail(trimmed)) {
      String email = IdentifierNormalizer.normalizeEmail(trimmed);
      User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
      return AppUserDetails.from(user, email);
    }

    String matricNumber = IdentifierNormalizer.normalizeMatricNumber(trimmed);
    User user = userRepository.findByMatricNumber(matricNumber)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return AppUserDetails.from(user, matricNumber);
  }
}