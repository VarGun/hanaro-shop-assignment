package com.example.hanaro.controller;

import com.example.hanaro.dto.LoginRequest;
import com.example.hanaro.dto.LoginResponse;
import com.example.hanaro.dto.UserSignUpRequest;
import com.example.hanaro.entity.Role;
import com.example.hanaro.entity.User;
import com.example.hanaro.repository.UserRepository;
import com.example.hanaro.security.CustomUserDetails;
import com.example.hanaro.security.JwtTokenProvider;
import com.example.hanaro.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final UserService userService;
  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/signup")
  public ResponseEntity<Void> signUp(@Valid @RequestBody UserSignUpRequest req) {
    userService.signUp(req, Role.USER);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

    String token = jwtTokenProvider.createToken((UserDetails) auth.getPrincipal());
    return ResponseEntity.ok(new TokenResponse(token, "Bearer"));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request,
      HttpServletResponse response) {

    new SecurityContextLogoutHandler()
        .logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    Cookie c = new Cookie("JSESSIONID", null);
    c.setPath("/");
    c.setHttpOnly(true);
    c.setMaxAge(0);
    response.addCookie(c);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public LoginResponse me(@AuthenticationPrincipal CustomUserDetails principal) {

    User u = principal.getUser();
    return new LoginResponse(u.getId(), u.getEmail(), u.getName(), u.getRole());
  }

  @Data
  @AllArgsConstructor
  public static class TokenResponse {

    private String accessToken;
    private String tokenType;
  }

}