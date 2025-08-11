package com.example.hanaro.controller;

import com.example.hanaro.dto.OrderResponse;
import com.example.hanaro.entity.OrderStatus;
import com.example.hanaro.service.OrderService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminOrderController {

  private final OrderService orderService;

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/api/admin/orders/{orderId}/status")
  public ResponseEntity<Void> changeStatus(
      @PathVariable Long orderId,
      @RequestParam OrderStatus status
  ) {
    orderService.changeStatusByAdmin(orderId, status);  // CANCELED 시 재고 롤백 포함
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
}
