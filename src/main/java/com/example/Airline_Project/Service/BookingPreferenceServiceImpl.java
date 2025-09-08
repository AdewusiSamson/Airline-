package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.BookingPreferenceRepository;
import com.example.Airline_Project.model.BookingPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingPreferenceServiceImpl implements BookingPrefrenceService {

    @Autowired
    private BookingPreferenceRepository bookingPreferenceRepository;

    @Override
    public BookingPreference savePreference(BookingPreference preference) {
        return bookingPreferenceRepository.save(preference);
    }

    @Override
    public Optional<BookingPreference> findByBookingId(Long bookingId) {
        return bookingPreferenceRepository.findByBookingId(bookingId);
    }

    @Override
    public void deletePreference(Long id) {
        bookingPreferenceRepository.deleteById(id);
    }
}