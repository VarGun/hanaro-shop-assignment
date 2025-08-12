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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger bizOrderLog = LoggerFactory.getLogger("business.order");
  private static final Logger bizProductLog = LoggerFactory.getLogger("business.product"); // 추가

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final DailySalesStatRepository dailySalesStatRepository;
  private final DailyProductStatRepository dailyProductStatRepository;

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

      // 재고 차감 + 로그
      int before = p.getStockQuantity();
      p.decreaseStock(qty);
      int after = p.getStockQuantity();
      productRepository.save(p);

      bizProductLog.info(
          "[STOCK][ORDER][DECREASE] productId={}, name='{}', before={}, delta=-{}, after={}",
          p.getId(), p.getName(), before, qty, after);
    }

    order.updateTotalPrice(total);

    Order saved = orderRepository.save(order);

    cart.getCartItems().clear();

    bizOrderLog.info("[CREATE] orderId={}, userId={}, totalPrice={}", saved.getId(),
        saved.getUser() != null ? saved.getUser().getId() : null, saved.getTotalPrice());
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
   * 사용자: 주문 취소 (본인만, 허용 상태에서만) - 예: ORDERED/READY/SHIPPING까지만 취소 허용 - 재고 복원
   */
  @Transactional
  public void cancel(Long orderId, Long userId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));
    if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("본인 주문만 취소할 수 있습니다.");
    }

    if (!(order.getStatus() == OrderStatus.ORDERED || order.getStatus() == OrderStatus.READY
        || order.getStatus() == OrderStatus.SHIPPING)) {
      throw new IllegalStateException("취소할 수 없는 상태입니다. (" + order.getStatus() + ")");
    }

    if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
      return;
    }

    // 재고 복원
    bizOrderLog.info("[CANCEL][USER] orderId={}, userId={}, statusBefore={}", orderId, userId,
        order.getStatus());
    for (OrderItem oi : order.getOrderItems()) {
      if (oi == null) {
        continue;
      }
      Product p = oi.getProduct();
      if (p != null) {
        int before = p.getStockQuantity();
        p.increaseStock(oi.getQuantity());
        int after = p.getStockQuantity();
        productRepository.save(p);
        bizProductLog.info(
            "[STOCK][USER-CANCEL][INCREASE] productId={}, name='{}', before={}, delta=+{}, after={}, orderId={}, userId={}",
            p.getId(), p.getName(), before, oi.getQuantity(), after, order.getId(), userId);
      }
    }

    order.changeStatus(OrderStatus.CANCELED);
  }


  private boolean isValidTransition(OrderStatus current, OrderStatus target) {
    if (current == target) {
      return true;
    }
    switch (current) {
      case ORDERED:
        return target == OrderStatus.READY || target == OrderStatus.CANCELED;
      case READY:
        return target == OrderStatus.SHIPPING || target == OrderStatus.CANCELED;
      case SHIPPING:
        return target == OrderStatus.COMPLETED || target == OrderStatus.CANCELED;
      case COMPLETED:
      case CANCELED:
      default:
        return false;
    }
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> listByUser(Long userId, Pageable pageable) {
    return orderRepository.findByUser_Id(userId, pageable)
        .map(OrderResponse::from);
  }

  @Scheduled(cron = "0 5 * * * *")
  @Transactional
  public void moveOrderedToReady() {
    LocalDateTime threshold = LocalDateTime.now().minusMinutes(5); // 5분 체류 보장 (운영)
    int updated = orderRepository.bulkUpdateStatusAfter(OrderStatus.ORDERED, OrderStatus.READY,
        threshold);
    log.info("[SCHED] ORDERED -> READY updated={}", updated);
  }

  @Scheduled(cron = "0 15 * * * *")
  @Transactional
  public void moveReadyToShipping() {
    LocalDateTime threshold = LocalDateTime.now().minusMinutes(15); // 15분 체류 보장 (운영)
    int updated = orderRepository.bulkUpdateStatusAfter(OrderStatus.READY, OrderStatus.SHIPPING,
        threshold);
    log.info("[SCHED] READY -> SHIPPING updated={}", updated);
  }

  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void moveShippingToCompleted() {
    LocalDateTime threshold = LocalDateTime.now().minusHours(1); // 1시간 체류 보장 (운영)
    int updated = orderRepository.bulkUpdateStatusAfter(OrderStatus.SHIPPING, OrderStatus.COMPLETED,
        threshold);
    log.info("[SCHED] SHIPPING -> COMPLETED updated={}", updated);
  }

  @Scheduled(cron = "0 0 0 * * *") // 매일 00:00, 전일 통계 집계
  @Transactional
  public void collectDailyStats() {
    LocalDate target = LocalDate.now().minusDays(1); // 전일
    LocalDateTime from = target.atStartOfDay();
    LocalDateTime to = target.plusDays(1).atStartOfDay();

    dailyProductStatRepository.deleteByDate(target);
    dailySalesStatRepository.deleteByDate(target);

    long orderCount = orderRepository.countByStatusAndDateBetween(OrderStatus.COMPLETED, from, to);
    Long totalAmount = orderRepository.sumTotalPriceByStatusAndDateBetween(OrderStatus.COMPLETED,
        from, to);
    if (totalAmount == null) {
      totalAmount = 0L;
    }

    dailySalesStatRepository.save(new DailySalesStat(target, orderCount, totalAmount));

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