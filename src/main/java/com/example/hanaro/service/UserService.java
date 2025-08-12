package com.example.hanaro.service;

import com.example.hanaro.dto.UserResponse;
import com.example.hanaro.dto.UserSignUpRequest;
import com.example.hanaro.dto.UserUpdateRequest;
import com.example.hanaro.entity.Role;
import com.example.hanaro.entity.User;
import com.example.hanaro.repository.UserRepository;
import com.example.hanaro.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


  @Transactional
  public UserResponse updateProfile(Long targetUserId,
      UserUpdateRequest req,
      CustomUserDetails principal) {

    // 권한: 본인 또는 ADMIN
    boolean isAdmin = principal.getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    boolean isOwner = principal.getId().equals(targetUserId);
    if (!(isAdmin || isOwner)) {
      throw new AccessDeniedException("본인 또는 관리자만 수정 가능합니다.");
    }

    // 대상 사용자
    User user = userRepository.findById(targetUserId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    if (req.email() != null && !req.email().equals(user.getEmail())) {
      throw new IllegalArgumentException("이메일은 변경할 수 없습니다.");
    }

    // 부분 수정(들어온 값만 반영)
    if (req.name() != null && !req.name().isBlank()) {
      user.setName(req.name().trim());
    }
    if (req.phone() != null && !req.phone().isBlank()) {
      user.setPhone(req.phone().trim());
    }
    if (req.password() != null && !req.password().isBlank()) {
      user.setPassword(passwordEncoder.encode(req.password()));
    }

    return UserResponse.from(user);
  }

}