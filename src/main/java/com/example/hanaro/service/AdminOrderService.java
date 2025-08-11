package com.example.hanaro.service;

import com.example.hanaro.dto.OrderResponse;
import com.example.hanaro.entity.Order;
import com.example.hanaro.entity.OrderStatus;
import com.example.hanaro.repository.OrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

  private final OrderRepository orderRepository;

  public List<OrderResponse> search(OrderStatus status,
      LocalDate fromDate,
      LocalDate toDate,
      String keyword) {

    LocalDateTime from = (fromDate == null) ? null : fromDate.atStartOfDay();
    LocalDateTime to = (toDate == null) ? null : toDate.plusDays(1).atStartOfDay(); // 반개구간

    String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

    List<Order> orders = orderRepository.searchForAdmin(status, from, to, kw);
    return orders.stream().map(OrderResponse::from).toList();
  }
}