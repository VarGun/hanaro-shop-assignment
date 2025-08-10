package com.example.hanaro.repository;


import com.example.hanaro.entity.Order;
import com.example.hanaro.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * 관리자용 주문 전체 조회 (조건: 상태, 기간)
   */
  @Query("""
      select o from Order o
      where (:status is null or o.status = :status)
        and (:fromDate is null or o.orderDate >= :fromDate)
        and (:toDate is null or o.orderDate <= :toDate)
      """)
  Page<Order> adminSearch(
      @Param("status") OrderStatus status,
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate,
      Pageable pageable
  );

  /**
   * 특정 사용자의 주문 전체 조회
   */
  @Query("""
      select o from Order o
      where o.user.id = :userId
        and (:status is null or o.status = :status)
        and (:fromDate is null or o.orderDate >= :fromDate)
        and (:toDate is null or o.orderDate <= :toDate)
      """)
  Page<Order> userSearch(
      @Param("userId") Long userId,
      @Param("status") OrderStatus status,
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate,
      Pageable pageable
  );

  List<Order> findByStatus(OrderStatus status);

  /**
   * 특정 사용자의 주문 목록 (페이징 없이)
   */
  List<Order> findByUser_Id(Long userId);
  
  @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
  Page<Order> findByUser_Id(Long userId, Pageable pageable);
}