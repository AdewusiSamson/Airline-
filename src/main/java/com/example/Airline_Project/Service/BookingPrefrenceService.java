package com.example.Airline_Project.Service;

import com.example.Airline_Project.model.BookingPreference;

import java.util.Optional;

public interface BookingPrefrenceService {
    BookingPreference savePreference(BookingPreference preference);
    Optional<BookingPreference> findByBookingId(Long bookingId);
    void deletePreference(Long id);
}
