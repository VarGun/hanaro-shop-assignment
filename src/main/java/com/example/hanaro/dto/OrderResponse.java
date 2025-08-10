package com.example.hanaro.dto;

import com.example.hanaro.entity.Order;
import com.example.hanaro.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record OrderResponse(
    Long id,
    Long userId,
    LocalDateTime orderDate,
    OrderStatus status,
    int totalPrice,
    List<OrderLine> items
) {

  public int getTotalPrice() {
    return totalPrice;
  }

  @Builder
  public record OrderLine(Long productId, String name, int price, int quantity) {

  }


  public static OrderResponse from(Order order) {
    List<OrderLine> lines = order.getOrderItems().stream()
        .map(oi -> OrderLine.builder()
            .productId(oi.getProduct().getId())
            .name(oi.getProduct().getName())
            .price(oi.getPrice())
            .quantity(oi.getQuantity())
            .build())
        .toList();

    return OrderResponse.builder()
        .id(order.getId())
        .userId(order.getUser().getId())
        .orderDate(order.getOrderDate())
        .status(order.getStatus())
        .totalPrice(order.getTotalPrice())
        .items(lines)
        .build();
  }
}