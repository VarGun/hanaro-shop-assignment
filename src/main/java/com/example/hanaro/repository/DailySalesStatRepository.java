package com.example.hanaro.repository;

import com.example.hanaro.entity.DailySalesStat;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailySalesStatRepository extends JpaRepository<DailySalesStat, LocalDate> {

  List<DailySalesStat> findByStatDateBetweenOrderByStatDateAsc(LocalDate from, LocalDate to);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("delete from DailySalesStat d where d.statDate = :date")
  int deleteByDate(@Param("date") LocalDate date);
}