package com.ellectronic.voting.storage.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
  
  @Bean 
  public Cloudinary cloudinary(
    @Value("${cloudinary.cloud-name}") String cloudName, 
    @Value("${cloudinary.api-key}") String apiKey, 
    @Value("${cloudinary.api-secret}") String apiSecret
  ){
    return new Cloudinary(
      ObjectUtils.asMap(
        "cloud-name",cloudName,
        "api-key", apiKey,
        "api-secret", apiSecret,
        "secure", true
      )
    );
  }
}
