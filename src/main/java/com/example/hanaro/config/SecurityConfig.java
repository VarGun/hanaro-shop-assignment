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
        .csrf(csrf -> csrf
            .disable()
        )
        .headers(h -> h.frameOptions(f -> f.disable())) // H2 콘솔
        .authenticationProvider(authenticationProvider())
        .authorizeHttpRequests(auth -> auth
            // Swagger & OpenAPI (public)
            .requestMatchers(
                "/v3/**",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/webjars/**"
            ).permitAll()

            // Public
            .requestMatchers(
                "/api/auth/login",
                "/api/auth/signup",
                "/api/auth/signup-admin",
                "/h2-console/**",
                "/uploads/**"
            ).permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

            // Admin only
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Others require auth
            .anyRequest().authenticated()
        )
        .formLogin(f -> f.disable())
        .httpBasic(b -> b.disable());

    return http.build();
  }
}