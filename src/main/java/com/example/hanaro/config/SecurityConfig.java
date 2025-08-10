package com.example.hanaro.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final com.example.hanaro.security.CustomUserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration c)
      throws Exception {
    return c.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .headers(h -> h.frameOptions(f -> f.disable())) // H2 콘솔
        .authenticationProvider(authenticationProvider())
        .authorizeHttpRequests(auth -> auth
            // 공개
            .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/signup-admin")
            .permitAll()
            .requestMatchers("/h2-console/**", "/uploads/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

            // 관리자 전용
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // 나머지는 로그인 필요 (예: /api/auth/me, 장바구니/주문 등)
            .anyRequest().authenticated()
        )
        .formLogin(f -> f.disable())
        .httpBasic(b -> b.disable());

    return http.build();
  }
}