package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.Booking;
import com.example.Airline_Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);
    Optional<Booking> findByPnr(String pnr);
    Boolean existsByPnr(String pnr);
}