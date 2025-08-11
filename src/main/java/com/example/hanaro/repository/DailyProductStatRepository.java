package com.example.hanaro.repository;

import com.example.hanaro.entity.DailyProductStat;
import com.example.hanaro.entity.DailyProductStat.DailyProductStatId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyProductStatRepository extends
    JpaRepository<DailyProductStat, DailyProductStatId> {

}