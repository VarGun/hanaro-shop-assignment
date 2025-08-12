package com.example.hanaro.repository;

import com.example.hanaro.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  // 같은 상품이 이미 담겨있는지 확인
  boolean existsByCart_IdAndProduct_Id(Long cartId, Long productId);

  boolean existsByProduct_Id(Long productId);
}