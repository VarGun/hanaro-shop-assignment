package com.example.hanaro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "daily_product_stat")
public class DailyProductStat extends BaseEntity {

  @EmbeddedId
  private DailyProductStatId id;

  @Column(name = "quantity", nullable = false)
  private long quantity;

  @Column(name = "amount", nullable = false)
  private long amount;

  protected DailyProductStat() {
  }

  public DailyProductStat(LocalDate date, Long productId, long quantity, long amount) {
    this.id = new DailyProductStatId(date, productId);
    this.quantity = quantity;
    this.amount = amount;
  }

  public DailyProductStatId getId() {
    return id;
  }

  public long getQuantity() {
    return quantity;
  }

  public long getAmount() {
    return amount;
  }

  public LocalDate getStatDate() {
    return id.statDate;
  }

  public Long getProductId() {
    return id.productId;
  }

  @Embeddable
  public static class DailyProductStatId implements Serializable {

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    protected DailyProductStatId() {
    }

    public DailyProductStatId(LocalDate statDate, Long productId) {
      this.statDate = statDate;
      this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DailyProductStatId that)) {
        return false;
      }
      return Objects.equals(statDate, that.statDate) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(statDate, productId);
    }
  }
}