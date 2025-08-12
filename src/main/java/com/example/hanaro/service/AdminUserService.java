package com.example.hanaro.service;

import com.example.hanaro.dto.UserResponse;
import com.example.hanaro.entity.User;
import com.example.hanaro.repository.CartRepository;
import com.example.hanaro.repository.OrderRepository;
import com.example.hanaro.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;


  @Transactional
  public void delete(Long userId) {
    boolean hasOrders = orderRepository.existsByUser_Id(userId);
    boolean hasCartItems = cartRepository.existsByUser_Id(userId);

    if (hasOrders || hasCartItems) {
      throw new IllegalStateException("연결된 주문/장바구니 때문에 삭제할 수 없습니다.");
    }

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
