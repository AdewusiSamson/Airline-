package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.AnalyticsRepository;
import com.example.Airline_Project.Repository.BookingRepository;
import com.example.Airline_Project.Repository.UserRepository;
import com.example.Airline_Project.model.Analytics;
import com.example.Airline_Project.model.Booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Analytics getAnalyticsForDate(LocalDate date) {
        return analyticsRepository.findByDate(date)
                .orElseGet(() -> createDefaultAnalytics(date));
    }

    @Override
    public List<Analytics> getAnalyticsForDateRange(LocalDate startDate, LocalDate endDate) {
        return analyticsRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public double calculateRevenue(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByBookingDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        return bookings.stream()
                .mapToDouble(Booking::getTotalAmount)
                .sum();
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Override
    public void updateDailyAnalytics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Check if analytics already exists for yesterday
        Optional<Analytics> existingAnalytics = analyticsRepository.findByDate(yesterday);
        if (existingAnalytics.isPresent()) {
            return; // Already updated
        }

        Analytics analytics = new Analytics();
        analytics.setDate(yesterday);

        // Calculate daily stats
        List<Booking> dailyBookings = bookingRepository.findByBookingDateBetween(
                yesterday.atStartOfDay(),
                yesterday.atTime(23, 59, 59)
        );

        analytics.setTotalBookings(dailyBookings.size());
        analytics.setTotalRevenue(dailyBookings.stream()
                .mapToDouble(Booking::getTotalAmount)
                .sum());

        analytics.setNewUsers(userRepository.countByCreatedDateBetween(
                yesterday.atStartOfDay(),
                yesterday.atTime(23, 59, 59)
        ));

        analytics.setFlightsTaken(dailyBookings.stream()
                .mapToInt(booking -> booking.getFlight().getSize())
                .sum());

        // Find most popular route (simplified)
        if (!dailyBookings.isEmpty()) {
            analytics.setMostPopularRoute("JFK-LHR"); // This would be calculated from actual data
        }

        analyticsRepository.save(analytics);
    }

    private Analytics createDefaultAnalytics(LocalDate date) {
        Analytics analytics = new Analytics();
        analytics.setDate(date);
        analytics.setTotalBookings(0);
        analytics.setTotalRevenue(0.0);
        analytics.setNewUsers(0);
        analytics.setFlightsTaken(0);
        analytics.setMostPopularRoute("N/A");
        return analytics;
    }
}