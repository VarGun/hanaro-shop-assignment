package com.example.hanaro.repository;

import com.example.hanaro.entity.DailyProductStat;
import com.example.hanaro.entity.DailyProductStat.DailyProductStatId;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyProductStatRepository extends
    JpaRepository<DailyProductStat, DailyProductStatId> {

  @Query("select d from DailyProductStat d where d.id.statDate between :from and :to order by d.id.statDate asc")
  List<DailyProductStat> findByStatDateBetweenOrderByStatDateAsc(@Param("from") LocalDate from,
      @Param("to") LocalDate to);
}