package com.example.hanaro.service;

import com.example.hanaro.dto.UserResponse;
import com.example.hanaro.entity.User;
import com.example.hanaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<UserResponse> list(Pageable pageable) {
    return userRepository.findAll(pageable).map(UserResponse::from);
  }

  @Transactional
  public void delete(Long userId) {
    // 하드 삭제(요구사항 충족 최소안). 소프트 삭제 필요 시 flag 추가.
    User u = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    userRepository.delete(u);
  }

}
