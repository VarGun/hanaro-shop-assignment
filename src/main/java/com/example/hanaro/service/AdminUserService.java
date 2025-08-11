package com.example.hanaro.service;

import com.example.hanaro.dto.UserResponse;
import com.example.hanaro.entity.User;
import com.example.hanaro.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;

  @Transactional
  public void delete(Long userId) {
    User u = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    userRepository.delete(u);
  }

  public List<UserResponse> search(String q) {
    String keyword = (q == null || q.isBlank()) ? null : q.trim();
    return userRepository.searchForAdmin(keyword)
        .stream()
        .map(UserResponse::from)
        .toList();
  }
}
