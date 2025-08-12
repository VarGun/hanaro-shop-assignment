package com.example.hanaro.controller;

import com.example.hanaro.dto.OrderResponse;
import com.example.hanaro.security.CustomUserDetails;
import com.example.hanaro.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  // 장바구니를 주문으로 생성
  @PostMapping("/api/orders")
  public ResponseEntity<OrderResponse> createFromCart(
      @AuthenticationPrincipal CustomUserDetails user) {
    OrderResponse resp = orderService.createFromCart(user.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  // 주문 단건 조회 (본인 주문만 조회 가능)
  @GetMapping("/api/orders/{orderId}")
  public OrderResponse get(@PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails user) {
    return orderService.get(orderId, user.getId());
  }

  @PatchMapping("/api/orders/{orderId}/cancel")
  public ResponseEntity<Void> cancel(@PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails user) {
    orderService.cancel(orderId, user.getId());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/api/orders")
  public Page<OrderResponse> listMyOrders(
      @AuthenticationPrincipal CustomUserDetails user,
      @ParameterObject
      @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    log.info("[USER][ORDERS][LIST] page={}, size={}, sort={}",
        pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    return orderService.listByUser(user.getId(), pageable);
  }
}