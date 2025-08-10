package com.example.hanaro.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  private LocalDateTime orderDate;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  private int totalPrice;

  @Column(nullable = false)
  private LocalDateTime statusChangedAt;


  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  @Builder
  public Order(User user) {
    this.user = user;
    this.status = OrderStatus.ORDERED;
    this.orderDate = LocalDateTime.now();
    this.statusChangedAt = this.orderDate;
    // orderItems는 필드에서 이미 new ArrayList<>()로 초기화되어 있음
    // totalPrice는 아이템 추가 후 서비스에서 updateTotalPrice(...)로 반영
  }

  public int calculateTotalPrice() {
    if (orderItems == null || orderItems.isEmpty()) {
      return 0;
    }
    return orderItems.stream()
        .mapToInt(oi -> (oi.getPrice() * oi.getQuantity()))
        .sum();
  }

  public void changeStatus(OrderStatus status) {
    this.status = status;
    this.statusChangedAt = LocalDateTime.now();
  }

  public void updateTotalPrice(int totalPrice) {
    this.totalPrice = totalPrice;
  }

  public void addOrderItem(OrderItem item) {
    if (item == null) {
      return;
    }
    if (this.orderItems == null) {
      this.orderItems = new ArrayList<>();
    }
    this.orderItems.add(item);
    item.setOrder(this);
  }

  public List<OrderItem> getOrderItems() {
    return orderItems != null ? orderItems : Collections.emptyList(); // 방어적 반환(선택)
  }

}