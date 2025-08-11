package com.example.hanaro.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CartItem> cartItems = new ArrayList<>();

  public Cart(User user, List<CartItem> cartItems) {
    this.user = user;
    if (cartItems != null) {
      this.cartItems = new ArrayList<>(cartItems);
    }
  }

  public void addItem(Product product, int quantity) {
    if (product == null) {
      return;
    }
    if (quantity <= 0) {
      return;
    }

    // 이미 담은 상품이면 수량 합산 (orphanRemoval 전략을 이용해 교체)
    CartItem existing = this.cartItems.stream()
        .filter(ci -> ci.getProduct() != null && ci.getProduct().getId().equals(product.getId()))
        .findFirst()
        .orElse(null);

    if (existing != null) {
      int newQty = existing.getQuantity() + quantity;
      this.cartItems.remove(existing);
      CartItem merged = CartItem.builder()
          .cart(this)
          .product(product)
          .quantity(newQty)
          .build();
      this.cartItems.add(merged);
    } else {
      CartItem item = CartItem.builder()
          .cart(this)
          .product(product)
          .quantity(quantity)
          .build();
      this.cartItems.add(item);
    }
  }

  public void removeItemByProductId(Long productId) {
    this.cartItems.removeIf(ci -> ci.getProduct().getId().equals(productId));
  }

  public void changeQuantity(Long productId, int quantity) {
    if (productId == null) {
      return;
    }
    CartItem existing = this.cartItems.stream()
        .filter(ci -> ci.getProduct() != null && ci.getProduct().getId().equals(productId))
        .findFirst()
        .orElse(null);
    if (existing == null) {
      return;
    }

    this.cartItems.remove(existing);
    if (quantity > 0) {
      CartItem updated = CartItem.builder()
          .cart(this)
          .product(existing.getProduct())
          .quantity(quantity)
          .build();
      this.cartItems.add(updated);
    }
  }

  public void clearItems() {
    this.cartItems.clear();
  }

}