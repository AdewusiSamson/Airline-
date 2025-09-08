package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.Booking;
import com.example.Airline_Project.model.BookingPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingPreferenceRepository extends JpaRepository<BookingPreference, Long> {
    Optional<BookingPreference> findByBookingId(Long bookingId);
}
