package com.example.hanaro.controller;

import com.example.hanaro.dto.OrderResponse;
import com.example.hanaro.entity.OrderStatus;
import com.example.hanaro.security.CustomUserDetails;
import com.example.hanaro.service.OrderService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  /**
   * 장바구니를 주문으로 생성
   */
  @PostMapping("/api/orders")
  public ResponseEntity<OrderResponse> createFromCart(
      @AuthenticationPrincipal CustomUserDetails user) {
    log.info(" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@[ORDER][CREATE] start userId={}",
        user != null ? user.getId() : null);
    OrderResponse resp = orderService.createFromCart(user.getId());
    log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ [ORDER][CREATE] success userId={} orderTotal={}",
        user.getId(),
        resp != null ? resp.getTotalPrice() : null);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  /**
   * 주문 단건 조회 (본인 주문만 조회 가능)
   */
  @GetMapping("/api/orders/{orderId}")
  public OrderResponse get(@PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails user) {
    return orderService.get(orderId, user.getId());
  }

  /**
   * 주문 상태 변경
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/api/admin/orders/{orderId}/status")
  public ResponseEntity<Void> changeStatus(
      @PathVariable Long orderId,
      @RequestParam OrderStatus status
  ) {
    orderService.changeStatus(orderId, status);
    return ResponseEntity.ok().build();
  }


  @PatchMapping("/api/orders/{orderId}/cancel")
  public ResponseEntity<Void> cancel(@PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails user) {
    orderService.cancel(orderId, user.getId());
    return ResponseEntity.ok().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(value = "/api/admin/orders", params = "!userId")
  public Page<OrderResponse> search(
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    return orderService.adminSearch(status, from, to, pageable);
  }

  @GetMapping("/api/orders")
  public Page<OrderResponse> listMyOrders(
      @AuthenticationPrincipal CustomUserDetails user,
      @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    return orderService.listByUser(user.getId(), pageable);
  }
}