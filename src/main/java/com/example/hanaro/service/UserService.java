package com.example.hanaro.service;

import com.example.hanaro.dto.UserSignUpRequest;
import com.example.hanaro.entity.Role;
import com.example.hanaro.entity.User;
import com.example.hanaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public Long signUp(UserSignUpRequest req, Role role) {
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new IllegalStateException("이미 가입된 이메일입니다.");
    }
    User saved = userRepository.save(User.builder()
        .email(req.getEmail())
        .password(passwordEncoder.encode(req.getPassword()))
        .name(req.getName())
        .phone(req.getPhone())
        .role(role == null ? Role.USER : role)
        .build());
    return saved.getId();
  }
}