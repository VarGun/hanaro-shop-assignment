package com.example.hanaro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private int price;

  private String description;

  @Setter
  private int stockQuantity;

  private String imageUrl;

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  @LastModifiedDate
  private LocalDateTime updatedAt;


  @Builder
  public Product(String name, int price, String description, int stockQuantity, String imageUrl) {
    this.name = name;
    this.price = price;
    this.description = description;
    this.stockQuantity = stockQuantity;
    this.imageUrl = imageUrl;
  }

  public void decreaseStock(int quantity) {
    if (this.stockQuantity < quantity) {
      throw new IllegalArgumentException("재고가 부족합니다.");
    }
    this.stockQuantity -= quantity;
  }

  public void increaseStock(int quantity) {
    this.stockQuantity += quantity;
  }

  public void changeInfo(String name, String desc, int price, int stock) {
    this.name = name;
    this.description = desc;
    this.price = price;
    this.stockQuantity = stock;
  }

  public void changeImage(String imageUrl) {
    this.imageUrl = imageUrl;
  }

}