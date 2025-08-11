package com.example.hanaro.config;

import com.example.hanaro.security.JwtAuthenticationFilter;
import com.example.hanaro.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final com.example.hanaro.security.CustomUserDetailsService userDetailsService;
  private final JwtTokenProvider jwtTokenProvider;

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
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(jsonAuthEntryPoint())
            .accessDeniedHandler(jsonAccessDeniedHandler())
        )
        .authorizeHttpRequests(auth -> auth
            // Swagger & OpenAPI (public)
            .requestMatchers(
                "/v3/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/actuator/health",
                "/actuator/metrics/**"
            ).permitAll()

            // Public
            .requestMatchers(
                "/api/auth/login",
                "/api/auth/signup",
                "/api/auth/signup-admin",
                "/h2-console/**",
                "/uploads/**",
                "/resources/**",
                "/static/**"
            ).permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

            // Admin only
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Others require auth
            .anyRequest().authenticated()
        )
        .formLogin(f -> f.disable())
        .httpBasic(b -> b.disable());

    http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
        UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationEntryPoint jsonAuthEntryPoint() {
    return (request, response, authException) -> {
      response.setStatus(401);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}");
    };
  }

  @Bean
  public AccessDeniedHandler jsonAccessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      response.setStatus(403);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"접근 권한이 없습니다.\"}");
    };
  }
}