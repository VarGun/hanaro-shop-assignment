package com.example.hanaro.controller;

import com.example.hanaro.dto.OrderResponse;
import com.example.hanaro.entity.OrderStatus;
import com.example.hanaro.service.AdminOrderService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

  private final AdminOrderService adminOrderService;

  @GetMapping
  public List<OrderResponse> search(
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
      @RequestParam(required = false) String keyword
  ) {
    return adminOrderService.search(status, fromDate, toDate, keyword);
  }
}