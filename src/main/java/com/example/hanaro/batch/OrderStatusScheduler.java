// OrderStatusScheduler.java
package com.example.hanaro.batch;

import com.example.hanaro.entity.Order;
import com.example.hanaro.entity.OrderStatus;
import com.example.hanaro.repository.OrderRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OrderStatusScheduler {

  private final OrderRepository orderRepository;

  @Transactional
  @Scheduled(fixedDelay = 60_000) // 1분마다 실행
  public void advanceOrderStatuses() {
    LocalDateTime now = LocalDateTime.now();

    // ORDERED → SHIPPING (5분 후)
    List<Order> orderedList = orderRepository.findByStatus(OrderStatus.ORDERED);
    orderedList.stream()
        // 일단 10 초로
        .filter(o -> Duration.between(o.getStatusChangedAt(), now).getSeconds() >= 10)
//        .filter(o -> Duration.between(o.getStatusChangedAt(), now).toMinutes() >= 5)
        .forEach(o -> {
          o.changeStatus(OrderStatus.SHIPPING);
          log.info("[SCHED] ORDER {} -> SHIPPING", o.getId());
        });

    // SHIPPING → COMPLETED (60분 후)
    List<Order> shippingList = orderRepository.findByStatus(OrderStatus.SHIPPING);
    shippingList.stream()
        .filter(o -> Duration.between(o.getStatusChangedAt(), now).toMinutes() >= 60)
        .forEach(o -> {
          o.changeStatus(OrderStatus.COMPLETED);
          log.info("[SCHED] ORDER {} -> COMPLETED", o.getId());
        });
  }
}