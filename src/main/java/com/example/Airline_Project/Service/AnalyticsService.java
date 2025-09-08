package com.example.Airline_Project.Service;

import com.example.Airline_Project.model.Analytics;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
    Analytics getAnalyticsForDate(LocalDate date);
    List<Analytics> getAnalyticsForDateRange(LocalDate startDate, LocalDate endDate);
    double calculateRevenue(LocalDate startDate, LocalDate endDate);
    void updateDailyAnalytics();
}
