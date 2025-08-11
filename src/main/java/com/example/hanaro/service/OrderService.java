package com.example.hanaro.service;

import com.example.hanaro.dto.OrderResponse;
import com.example.hanaro.entity.Cart;
import com.example.hanaro.entity.CartItem;
import com.example.hanaro.entity.DailyProductStat;
import com.example.hanaro.entity.DailySalesStat;
import com.example.hanaro.entity.Order;
import com.example.hanaro.entity.OrderItem;
import com.example.hanaro.entity.OrderStatus;
import com.example.hanaro.entity.Product;
import com.example.hanaro.repository.CartRepository;
import com.example.hanaro.repository.DailyProductStatRepository;
import com.example.hanaro.repository.DailySalesStatRepository;
import com.example.hanaro.repository.OrderRepository;
import com.example.hanaro.repository.ProductRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final DailySalesStatRepository dailySalesStatRepository;
  private final DailyProductStatRepository dailyProductStatRepository;

  /**
   * 장바구니 → 주문 생성 - 장바구니 존재/비어있음/재고 검증 - 주문 아이템 생성 및 총액 계산 - 재고 차감 - 장바구니 비우기
   */
  @Transactional
  public OrderResponse createFromCart(Long userId) {
    Cart cart = cartRepository.findByUser_Id(userId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니가 없습니다."));

    if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
      throw new IllegalArgumentException("장바구니가 비어 있습니다.");
    }

    // 재고 사전 검증
    for (CartItem ci : cart.getCartItems()) {
      Product p = ci.getProduct();
      if (p == null) {
        throw new IllegalArgumentException("유효하지 않은 상품이 포함되어 있습니다.");
      }
      if (p.getStockQuantity() < ci.getQuantity()) {
        throw new IllegalStateException("재고 부족: " + p.getName());
      }
    }

    // 주문 생성
    Order order = Order.builder()
        .user(cart.getUser())
        .build();

    int total = 0;
    List<OrderItem> orderItems = new ArrayList<>();
    for (CartItem ci : cart.getCartItems()) {
      Product p = ci.getProduct();
      int unitPrice = p.getPrice();
      int qty = ci.getQuantity();

      OrderItem oi = OrderItem.builder()
          .product(p)
          .price(unitPrice)
          .quantity(qty)
          .build();

      order.addOrderItem(oi);
      total += unitPrice * qty;

      // 재고 차감
      p.decreaseStock(qty);
      productRepository.save(p); // 재고 반영
    }

    // 연관관계 설정 (Order ↔ OrderItem)
    // Order 엔티티에 items 컬렉션이 있고 Cascade.ALL이라면 save(order)만으로 전파됨

    order.updateTotalPrice(total);

    Order saved = orderRepository.save(order);

    // 장바구니 비우기(또는 삭제)
    cart.getCartItems().clear();
    // cartRepository.delete(cart); // 장바구니 자체 삭제를 원하면 이 방식으로 전환
    // 비우기만 할 거면 아래 저장 유지
    // cartRepository.save(cart);

    return OrderResponse.from(saved);
  }

  /**
   * 주문 단건 조회 (본인만)
   */
  @Transactional(readOnly = true)
  public OrderResponse get(Long orderId, Long userId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));
    if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("본인 주문만 조회할 수 있습니다.");
    }
    return OrderResponse.from(order);
  }

  /**
   * 본인 주문 전체 조회
   */
  @Transactional(readOnly = true)
  public List<OrderResponse> listByUser(Long userId) {
    return orderRepository.findByUser_Id(userId).stream()
        .map(OrderResponse::from)
        .toList();
  }

  /**
   * 관리자 검색 (전체 사용자 대상)
   */
  @Transactional(readOnly = true)
  public Page<OrderResponse> adminSearch(OrderStatus status,
      LocalDateTime from,
      LocalDateTime to,
      Pageable pageable) {
    return orderRepository.adminSearch(status, from, to, pageable)
        .map(OrderResponse::from);
  }

  /**
   * 관리자: 상태 변경 - 유효 전이만 허용(예시 정책)
   */
  @Transactional
  public void changeStatus(Long orderId, OrderStatus target) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));

    OrderStatus current = order.getStatus();
    if (!isValidTransition(current, target)) {
      throw new IllegalStateException("해당 상태로 변경할 수 없습니다. (" + current + " → " + target + ")");
    }

    order.changeStatus(target);
    // 배송중→취소 같은 정책을 허용하지 않는다면 위 isValidTransition에서 막힘
  }

  /**
   * 관리자: 상태 변경 (CANCELED 전환 시 재고 롤백 포함)
   */
  @Transactional
  public void changeStatusByAdmin(Long orderId, OrderStatus target) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));

    OrderStatus current = order.getStatus();

    // 완료된 주문은 더 이상 변경 불가(정책)
    if (current == OrderStatus.COMPLETED) {
      throw new IllegalStateException("배송완료 주문은 상태를 변경할 수 없습니다.");
    }

    if (!isValidTransition(current, target)) {
      throw new IllegalStateException("해당 상태로 변경할 수 없습니다. (" + current + " → " + target + ")");
    }

    // 관리자에 의해 CANCELED로 전환될 때 재고 롤백 보장
    if (target == OrderStatus.CANCELED && current != OrderStatus.CANCELED) {
      if (order.getOrderItems() != null) {
        for (OrderItem oi : order.getOrderItems()) {
          if (oi == null) {
            continue;
          }
          Product p = oi.getProduct();
          if (p != null) {
            p.increaseStock(oi.getQuantity());
            productRepository.save(p);
          }
        }
      }
    }

    order.changeStatus(target);
  }

  /**
   * 사용자: 주문 취소 (본인만, 허용 상태에서만) - 예: ORDERED/SHIPPING까지만 취소 허용 - 재고 복원
   */
  @Transactional
  public void cancel(Long orderId, Long userId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));
    if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("본인 주문만 취소할 수 있습니다.");
    }

    if (!(order.getStatus() == OrderStatus.ORDERED || order.getStatus() == OrderStatus.SHIPPING)) {
      throw new IllegalStateException("취소할 수 없는 상태입니다. (" + order.getStatus() + ")");
    }

    if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
      return;
    }

    // 재고 복원
    for (OrderItem oi : order.getOrderItems()) {
      if (oi == null) {
        continue;
      }
      Product p = oi.getProduct();
      if (p != null) {
        p.increaseStock(oi.getQuantity());
        productRepository.save(p);
      }
    }

    order.changeStatus(OrderStatus.CANCELED);
  }

  // -----------

  private boolean isValidTransition(OrderStatus current, OrderStatus target) {
    if (current == target) {
      return true;
    }
    switch (current) {
      case ORDERED:
        return target == OrderStatus.SHIPPING || target == OrderStatus.COMPLETED
            || target == OrderStatus.CANCELED;
      case SHIPPING:
        return target == OrderStatus.COMPLETED || target == OrderStatus.CANCELED;
      case COMPLETED:
      case CANCELED:
        return false;
      default:
        return false;
    }
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> listByUser(Long userId, Pageable pageable) {
    return orderRepository.findByUser_Id(userId, pageable)
        .map(OrderResponse::from);
  }

  //  @Scheduled(cron = "0 */5 * * * *") // 5분마다
  @Scheduled(cron = "*/15 * * * * *") // [LOCAL TEST] 15초마다 (ORDERED → SHIPPING)
  @Transactional
  public void moveOrderedToShipping() {
    int updated = orderRepository.bulkUpdateStatus(OrderStatus.ORDERED, OrderStatus.SHIPPING);
    log.info("[SCHED] ORDERED→SHIPPING updated={}", updated);
  }

  //  @Scheduled(cron = "0 0 * * * *") // 1시간마다
  @Scheduled(cron = "*/30 * * * * *") // [LOCAL TEST] 30초마다 (SHIPPING → COMPLETED)
  @Transactional
  public void moveShippingToCompleted() {
    int updated = orderRepository.bulkUpdateStatus(OrderStatus.SHIPPING, OrderStatus.COMPLETED);
    log.info("[SCHED] SHIPPING→COMPLETED updated={}", updated);
  }

  // 테스트용 (30초마다)
  @Scheduled(cron = "*/30 * * * * *") // [TEST] 30초마다
  //  @Scheduled(cron = "0 5 0 * * *") // 매일 00:05, 전일 통계 집계
  @Transactional
  public void collectDailyStats() {
//    LocalDate target = LocalDate.now().minusDays(1); // 전일
//    LocalDateTime from = target.atStartOfDay();
//    LocalDateTime to = target.plusDays(1).atStartOfDay(); // 반개방 구간 [from, to)

    // [테스트 전용] 오늘로 전환
    LocalDate target = LocalDate.now();
    LocalDateTime from = target.atStartOfDay();
    LocalDateTime to = target.plusDays(1).atStartOfDay();

    long orderCount = orderRepository.countByStatusAndDateBetween(OrderStatus.COMPLETED, from, to);
    Long totalAmount = orderRepository.sumTotalPriceByStatusAndDateBetween(OrderStatus.COMPLETED,
        from, to);
    if (totalAmount == null) {
      totalAmount = 0L;
    }

    // 집계 저장(일자 총계)
    dailySalesStatRepository.save(new DailySalesStat(target, orderCount, totalAmount));

    // 상품별 집계 저장
    java.util.List<Object[]> rows = orderRepository.productStatsByDate(OrderStatus.COMPLETED, from,
        to);
    int saved = 0;
    for (Object[] r : rows) {
      Long productId = (Long) r[0];
      long qty = ((Number) r[1]).longValue();
      long amt = ((Number) r[2]).longValue();
      dailyProductStatRepository.save(new DailyProductStat(target, productId, qty, amt));
      saved++;
    }

    log.info("[BATCH][DAILY] date={}, orders={}, totalAmount={}, productRows={}", target,
        orderCount, totalAmount, saved);
  }
}