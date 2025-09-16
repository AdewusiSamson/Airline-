package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.Airport;
import com.example.Airline_Project.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByOriginAndDestinationAndDepartureTimeBetween(
            Airport origin,
            Airport destination,
            LocalDateTime start,
            LocalDateTime end
    );
    Flight findByFlightId (Long flightId);
}
