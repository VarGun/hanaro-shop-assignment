package com.example.hanaro.repository;


import com.example.hanaro.entity.Order;
import com.example.hanaro.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * 관리자용 주문 전체 조회 (조건: 상태, 기간)
   */
  @Query("""
        select o
        from Order o join o.user u
        where (:status is null or o.status = :status)
          and (:from is null or o.orderDate >= :from)
          and (:to   is null or o.orderDate < :to)
          and (:keyword is null
               or lower(u.email) like lower(concat('%', :keyword, '%'))
               or lower(u.name)  like lower(concat('%', :keyword, '%'))
               or cast(o.id as string) like concat('%', :keyword, '%'))
      """)
  List<Order> searchForAdmin(@Param("status") OrderStatus status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("keyword") String keyword);

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

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Order o set o.status = :to where o.status = :from")
  int bulkUpdateStatus(@Param("from") OrderStatus from, @Param("to") OrderStatus to);

  @Query("select count(o) from Order o where o.status = :status and o.orderDate >= :from and o.orderDate < :to")
  long countByStatusAndDateBetween(@Param("status") OrderStatus status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  @Query("select coalesce(sum(o.totalPrice), 0) from Order o where o.status = :status and o.orderDate >= :from and o.orderDate < :to")
  Long sumTotalPriceByStatusAndDateBetween(@Param("status") OrderStatus status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  @Query("select oi.product.id, sum(oi.quantity), sum(oi.price * oi.quantity) " +
      "from OrderItem oi " +
      "where oi.order.status = :status and oi.order.orderDate >= :from and oi.order.orderDate < :to "
      +
      "group by oi.product.id " +
      "order by sum(oi.quantity) desc")
  List<Object[]> productStatsByDate(@Param("status") OrderStatus status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  @Modifying
  @Query("UPDATE Order o SET o.status = :to, o.updatedAt = CURRENT_TIMESTAMP " +
      "WHERE o.status = :from AND o.updatedAt <= :threshold")
  int bulkUpdateStatusAfter(@Param("from") OrderStatus from,
      @Param("to") OrderStatus to,
      @Param("threshold") LocalDateTime threshold);


  @Query("""
      select o
      from Order o
      join o.user u
      where (:status is null or o.status = :status)
        and (:from is null or o.orderDate >= :from)
        and (:to is null or o.orderDate < :to)
        and (
              :keyword is null
           or lower(u.email) like lower(concat('%', :keyword, '%'))
           or lower(u.name)  like lower(concat('%', :keyword, '%'))
           or cast(o.id as string) like concat('%', :keyword, '%')
        )
      """)
  Page<Order> searchForAdmin(
      @Param("status") OrderStatus status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("keyword") String keyword,
      Pageable pageable
  );
}