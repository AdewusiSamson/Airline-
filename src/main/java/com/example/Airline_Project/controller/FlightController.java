package com.example.Airline_Project.controller;

import com.example.Airline_Project.Service.FlightService;
import com.example.Airline_Project.model.Flight;
import com.example.Airline_Project.model.Seat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "http://localhost:5173")

public class FlightController {

    @Autowired
    private FlightService flightService;

    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String seatClass) {

        try {
            List<Flight> flights = flightService.searchFlights(origin, destination, date, seatClass);
            return ResponseEntity.ok(flights);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<Flight> getFlight(@PathVariable Long flightId) {
        try {
            Flight flight = flightService.getFlightById(flightId);
            return ResponseEntity.ok(flight);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{flightId}/seats")
    public ResponseEntity<List<Seat>> getFlightSeats(@PathVariable Long flightId) {
        try {
            List<Seat> seats = flightService.getSeatsForFlight(flightId);
            return ResponseEntity.ok(seats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
