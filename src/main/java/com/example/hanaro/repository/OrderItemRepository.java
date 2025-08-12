package com.example.hanaro.repository;

import com.example.hanaro.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  boolean existsByProduct_Id(Long productId);

}