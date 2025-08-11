package com.example.hanaro.dto;

import com.example.hanaro.entity.Order;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminOrderListItem {

  private Long id;
  private Long userId;
  private String userEmail;
  private LocalDateTime orderDate;
  private String status;
  private int totalPrice;
  private int itemCount;

  public static AdminOrderListItem from(Order o) {
    return AdminOrderListItem.builder()
        .id(o.getId())
        .userId(o.getUser().getId())
        .userEmail(o.getUser().getEmail())
        .orderDate(o.getOrderDate())
        .status(o.getStatus().name())
        .totalPrice(o.getTotalPrice())
        .itemCount(o.getOrderItems() == null ? 0 : o.getOrderItems().size())
        .build();
  }
}