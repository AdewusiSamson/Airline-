package com.example.Airline_Project.controller;

import com.example.Airline_Project.Service.AirportService;
import com.example.Airline_Project.model.Airport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@CrossOrigin(origins = "http://localhost:5173")

public class AirportController {

    @Autowired
    private AirportService airportService;

    @GetMapping
    public ResponseEntity<List<Airport>> getAllAirports() {
        try {
            List<Airport> airports = airportService.getAllAirports();
            return ResponseEntity.ok(airports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<Airport> getAirportByCode(@PathVariable String code) {
        try {
            Airport airport = airportService.getAirportByCode(code);
            return ResponseEntity.ok(airport);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}