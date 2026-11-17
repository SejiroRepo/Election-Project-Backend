package com.ellectronic.voting.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import java.util.Base64.Decoder;

@Service
public class JwtService {
  
  private final SecretKey signingKey;
  private final long accessTokenExpirationMs;

  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-token-expiration-ms}") Long accessTokenExpiratonMs){
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.accessTokenExpirationMs = accessTokenExpiratonMs;
  }


  public String generateAccessToken(UserDetails userDetails){
    Date issuedAt = new Date();
    Date expiration = new Date(issuedAt.getTime() + accessTokenExpirationMs);

    return Jwts.builder()
    .subject(userDetails.getUsername())
    .issuedAt(issuedAt)
    .expiration(expiration)
    .signWith(signingKey)
    .compact();
  }

  public String extractUsername(String token){
    return extractAllClaims(token).getSubject();
  }

  //Validate that token is for the authenticated user
  public boolean isTokenValid(String token, UserDetails userDetails){
    try{
      String username = extractUsername(token);
      return username.equals(userDetails.getUsername()) && userDetails.isEnabled() && !isTokenExpired(token);
    } 
    catch(io.jsonwebtoken.JwtException | IllegalArgumentException exception){
      return false;
    }
  }

  //Check if token is expired
  private boolean isTokenExpired(String token){
    return extractAllClaims(token).getExpiration().before(new Date());
  }

  //Extract all claims from token
  private Claims extractAllClaims(String token){
    return Jwts.parser()
      .verifyWith(signingKey)
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

}
