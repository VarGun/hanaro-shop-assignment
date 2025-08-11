package com.example.hanaro.repository;

import com.example.hanaro.entity.DailySalesStat;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySalesStatRepository extends JpaRepository<DailySalesStat, LocalDate> {

}