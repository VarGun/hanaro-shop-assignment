package com.example.hanaro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Setter;

@Entity
@Table(name = "daily_sales_stat")
public class DailySalesStat {

  @Id
  @Column(name = "stat_date", nullable = false)
  private LocalDate statDate;

  @Setter
  @Column(name = "order_count", nullable = false)
  private long orderCount;

  @Setter
  @Column(name = "total_amount", nullable = false)
  private long totalAmount;

  protected DailySalesStat() {
  }

  public DailySalesStat(LocalDate statDate, long orderCount, long totalAmount) {
    this.statDate = statDate;
    this.orderCount = orderCount;
    this.totalAmount = totalAmount;
  }

  public LocalDate getStatDate() {
    return statDate;
  }

  public long getOrderCount() {
    return orderCount;
  }

  public long getTotalAmount() {
    return totalAmount;
  }

}