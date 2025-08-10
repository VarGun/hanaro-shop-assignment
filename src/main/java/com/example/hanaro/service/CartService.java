package com.example.hanaro.service;

import com.example.hanaro.dto.AddToCartRequest;
import com.example.hanaro.dto.CartItemResponse;
import com.example.hanaro.dto.CartResponse;
import com.example.hanaro.entity.Cart;
import com.example.hanaro.entity.Product;
import com.example.hanaro.entity.User;
import com.example.hanaro.repository.CartItemRepository;
import com.example.hanaro.repository.CartRepository;
import com.example.hanaro.repository.ProductRepository;
import com.example.hanaro.repository.UserRepository;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  /**
   * 장바구니에 상품 담기 (같은 상품이면 수량 증가)
   */
  public void addToCart(Long userId, AddToCartRequest req) {
    if (req.getQuantity() == null || req.getQuantity() <= 0) {
      throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
    }

    Cart cart = cartRepository.findByUser_Id(userId)
        .orElseGet(() -> {
          User user = userRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));
          return cartRepository.save(Cart.builder().user(user).build());
        });

    Product product = productRepository.findById(req.getProductId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품"));

    int existingQty = cart.getCartItems().stream()
        .filter(ci -> ci.getProduct().getId().equals(product.getId()))
        .mapToInt(ci -> ci.getQuantity())
        .findFirst()
        .orElse(0);

    int newTotal = existingQty + req.getQuantity();
    if (product.getStockQuantity() < newTotal) {
      throw new IllegalStateException("재고가 부족합니다.");
    }

    // 도메인 메서드가 동일 상품 병합(수량 합산)을 처리
    cart.addItem(product, req.getQuantity());
  }

  @Transactional
  public void addAllToCart(Long userId, List<AddToCartRequest> reqList) {
    if (reqList == null || reqList.isEmpty()) {
      throw new IllegalArgumentException("추가할 항목이 없습니다.");
    }
    for (AddToCartRequest req : reqList) {
      addToCart(userId, req); // 단건 로직(재고/병합 검증) 재사용
    }
  }

  /**
   * 장바구니 수량 변경(배치) - 동일 상품이 여러 번 오면 마지막 값을 우선 적용
   */
  public void changeQuantities(Long userId, List<AddToCartRequest> reqList) {
    if (reqList == null || reqList.isEmpty()) {
      throw new IllegalArgumentException("변경할 항목이 없습니다.");
    }

    Cart cart = cartRepository.findByUser_Id(userId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니가 없습니다."));

    // 동일 productId가 여러 번 오면 마지막 값으로 덮어쓰기
    Map<Long, Integer> desired = new LinkedHashMap<>();
    for (AddToCartRequest r : reqList) {
      if (r.getProductId() == null) {
        continue;
      }
      desired.put(r.getProductId(), r.getQuantity());
    }

    for (Map.Entry<Long, Integer> e : desired.entrySet()) {
      Long productId = e.getKey();
      Integer quantity = e.getValue();

      if (quantity == null || quantity <= 0) {
        cart.removeItemByProductId(productId);
        continue;
      }

      Product product = productRepository.findById(productId)
          .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품"));

      if (product.getStockQuantity() < quantity) {
        throw new IllegalStateException("재고가 부족합니다.");
      }

      cart.changeQuantity(productId, quantity);
    }
  }

  /**
   * 장바구니 내역 조회
   */
  @Transactional(readOnly = true)
  public CartResponse getItems(Long userId) {
    return cartRepository.findByUser_Id(userId)
        .map(cart -> {
          List<CartItemResponse> items = cart.getCartItems().stream()
              .map(ci -> new CartItemResponse(
                  ci.getProduct().getId(),
                  ci.getProduct().getName(),
                  ci.getProduct().getPrice(),
                  ci.getQuantity(),
                  ci.getProduct().getPrice() * ci.getQuantity()
              ))
              .toList();
          int total = items.stream().mapToInt(CartItemResponse::getTotalPrice).sum();
          return new CartResponse(items, total);
        })
        .orElse(new CartResponse(Collections.emptyList(), 0));
  }

  /**
   * 장바구니 수량 변경
   */
  public void changeQuantity(Long userId, Long productId, int quantity) {
    Cart cart = cartRepository.findByUser_Id(userId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니가 없습니다."));

    // 0 이하이면 해당 아이템 제거
    if (quantity <= 0) {
      cart.removeItemByProductId(productId);
      return;
    }

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품"));

    if (product.getStockQuantity() < quantity) {
      throw new IllegalStateException("재고가 부족합니다.");
    }

    // 도메인 메서드 사용(없으면 기존 아이템 미존재 시 예외 없이 무시)
    cart.changeQuantity(productId, quantity);
  }

  public void removeItem(Long userId, Long productId) {
    Cart cart = cartRepository.findByUser_Id(userId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니가 없습니다."));
    cart.removeItemByProductId(productId);
  }

  @Transactional
  public void deleteCart(Long userId) {
    cartRepository.findByUser_Id(userId).ifPresent(cartRepository::delete);
  }
}