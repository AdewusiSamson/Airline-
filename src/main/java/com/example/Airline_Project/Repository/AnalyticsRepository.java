package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {
    Optional<Analytics> findByDate(LocalDate date);

    @Query("SELECT a FROM Analytics a WHERE a.date BETWEEN :startDate AND :endDate ORDER BY a.date")
    List<Analytics> findByDateRange(LocalDate startDate, LocalDate endDate);
}