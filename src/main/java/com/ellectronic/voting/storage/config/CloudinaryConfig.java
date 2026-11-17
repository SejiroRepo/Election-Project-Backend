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
  ) {

      System.out.println("Cloudinary configuration check:");
      System.out.println("Cloud name present: " +
              (cloudName != null && !cloudName.isBlank()));
      System.out.println("API key present: " +
              (apiKey != null && !apiKey.isBlank()));
      System.out.println("API secret present: " +
              (apiSecret != null && !apiSecret.isBlank()));

      return new Cloudinary(
              ObjectUtils.asMap(
                      "cloud_name", cloudName,
                      "api_key", apiKey,
                      "api_secret", apiSecret,
                      "secure", true
              )
      );
  }
}
