package com.example.hanaro.controller;

import com.example.hanaro.entity.DailyProductStat;
import com.example.hanaro.entity.DailySalesStat;
import com.example.hanaro.repository.DailyProductStatRepository;
import com.example.hanaro.repository.DailySalesStatRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatController {

  private final DailySalesStatRepository dailySalesStatRepository;
  private final DailyProductStatRepository dailyProductStatRepository;

  @GetMapping("/sales")
  public List<SalesStatDto> sales(@RequestParam LocalDate from, @RequestParam LocalDate to) {
    return dailySalesStatRepository
        .findByStatDateBetweenOrderByStatDateAsc(from, to)
        .stream().map(SalesStatDto::from).toList();
  }

  @GetMapping("/products")
  public List<ProductStatDto> products(@RequestParam LocalDate from, @RequestParam LocalDate to) {
    return dailyProductStatRepository
        .findByStatDateBetweenOrderByStatDateAsc(from, to)
        .stream().map(ProductStatDto::from).toList();
  }

  // --- 내부 DTO ---
  public record SalesStatDto(LocalDate statDate, long orderCount, long totalAmount) {

    static SalesStatDto from(DailySalesStat s) {
      return new SalesStatDto(s.getStatDate(), s.getOrderCount(), s.getTotalAmount());
    }
  }

  public record ProductStatDto(LocalDate statDate, Long productId, long quantity, long amount) {

    static ProductStatDto from(DailyProductStat s) {
      return new ProductStatDto(s.getStatDate(), s.getProductId(), s.getQuantity(), s.getAmount());
    }
  }
}