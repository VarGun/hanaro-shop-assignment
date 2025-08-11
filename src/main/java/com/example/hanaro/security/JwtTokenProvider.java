package com.example.hanaro.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final UserDetailsService userDetailsService;

  public JwtTokenProvider(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Value("${app.jwt.secret}")
  private String secret;
  @Value("${app.jwt.access-token-validity}")
  private long validityInMs;

  private Key key;

  @PostConstruct
  void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String createToken(UserDetails principal) {
    String roles = principal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

    Date now = new Date();
    Date exp = new Date(now.getTime() + validityInMs);

    return Jwts.builder()
        .subject(principal.getUsername())
        .claim("roles", roles)
        .issuedAt(now)
        .expiration(exp)
        .signWith(key)
        .compact();
  }

  public boolean validate(String token) {
    try {
      Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public Authentication getAuthentication(String token) {
    String username = Jwts.parser().verifyWith((SecretKey) key).build()
        .parseSignedClaims(token).getPayload().getSubject();
    UserDetails user = userDetailsService.loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
  }
}