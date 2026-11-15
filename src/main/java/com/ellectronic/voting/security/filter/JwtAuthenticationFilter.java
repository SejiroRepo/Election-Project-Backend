package com.ellectronic.voting.security.filter;

import com.ellectronic.voting.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @Override 
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{

    // Get the Authorization header from the request
    String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

    // Continue without authentication when no bearer token exists.
    if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
        filterChain.doFilter(request, response);
        return;
    }

    // Extract the JWT token from the Authorization header
    String token = authorizationHeader.substring(BEARER_PREFIX.length());

    try{
      
      String matricNumber = jwtService.extractUsername(token);

      if(matricNumber != null && SecurityContextHolder.getContext().getAuthentication() == null){

        UserDetails userDetails = userDetailsService.loadUserByUsername(matricNumber);

        boolean isTokenValid = jwtService.isTokenValid(token, userDetails);
        if(isTokenValid){

          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authentication);

        }
      }
    } 
    catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
