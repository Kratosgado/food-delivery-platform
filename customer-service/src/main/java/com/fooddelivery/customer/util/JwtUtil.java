package com.fooddelivery.customer.util;

import com.fooddelivery.customer.model.Customer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final SecretKey secretKey;
  private final long expirationMs;

  public JwtUtil(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  public String generateToken(Customer customer) {
    return Jwts.builder()
        .subject(String.valueOf(customer.getId()))
        .claim("username", customer.getUsername())
        .claim("role", customer.getRole().name())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expirationMs))
        .signWith(secretKey)
        .compact();
  }
}
