package com.ellectronic.voting.security.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
  
  private final PasswordEncoder passwordEncoder;

  public PasswordService(PasswordEncoder passwordEncoder){
    this.passwordEncoder = passwordEncoder;
  }

  //Hash raw password
  public String encode(String rawPassword){

    if(rawPassword==null || rawPassword.isBlank()){
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    return passwordEncoder.encode(rawPassword);
  }

  //Compare raw password with hashed password
  public boolean matches(String rawPassword, String encodedPassword){
    if (rawPassword == null || encodedPassword == null) {
        return false;
    }

    return passwordEncoder.matches(rawPassword, encodedPassword);
  }
}
