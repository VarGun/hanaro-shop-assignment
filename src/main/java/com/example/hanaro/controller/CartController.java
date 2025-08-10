package com.example.hanaro.controller;

import com.example.hanaro.dto.AddToCartRequest;
import com.example.hanaro.dto.CartResponse;
import com.example.hanaro.security.CustomUserDetails;
import com.example.hanaro.service.CartService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartController {

  private final CartService cartService;

  // 인증 기반: 현재 로그인 사용자 기준으로 장바구니 처리
  @PostMapping("/items")
  public ResponseEntity<Void> addItems(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody List<AddToCartRequest> reqList
  ) {
    cartService.addAllToCart(user.getId(), reqList);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/items")
  public CartResponse getItems(@AuthenticationPrincipal CustomUserDetails user) {
    return cartService.getItems(user.getId());
  }

  @PatchMapping("/items/{productId}")
  public ResponseEntity<Void> changeQuantity(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long productId,
      @RequestParam int quantity
  ) {
    cartService.changeQuantity(user.getId(), productId, quantity);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/items")
  public ResponseEntity<Void> changeQuantities(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody List<AddToCartRequest> reqList
  ) {
    cartService.changeQuantities(user.getId(), reqList);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/items/{productId}")
  public ResponseEntity<Void> removeItem(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long productId
  ) {
    cartService.removeItem(user.getId(), productId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteCart(@AuthenticationPrincipal CustomUserDetails user) {
    cartService.deleteCart(user.getId());
    return ResponseEntity.noContent().build();
  }
}