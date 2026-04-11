package com.fooddelivery.gateway.filter;

import com.fooddelivery.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

  private final JwtUtil jwtUtil;

  /** Paths that skip JWT validation */
  private static final List<String> PUBLIC_PATHS =
      List.of("/api/customers/register", "/api/customers/login", "/actuator");

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    log.info("Request path: {}", path);

    if (isPublic(path)) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
    String token = authHeader.substring(7);

    if (!jwtUtil.isTokenValid(token)) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    // Forward user identity headers to downstream services
    Claims claims = jwtUtil.extractAllClaims(token);
    ServerWebExchange mutated =
        exchange
            .mutate()
            .request(
                r ->
                    r.header("X-User-Id", String.valueOf(claims.getSubject()))
                        .header("X-User-Role", String.valueOf(claims.get("role"))))
            .build();

    return chain.filter(mutated);
  }

  @Override
  public int getOrder() {
    return -1; // Run before routing filters
  }

  private boolean isPublic(String path) {
    return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
  }
}
