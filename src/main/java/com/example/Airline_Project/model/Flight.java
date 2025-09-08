package com.example.Airline_Project.model;

import com.example.Airline_Project.Domain.AircraftType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String flightNumber; // e.g., "BA025"

    @ManyToOne
    @JoinColumn(name = "airline_id", nullable = false)
    private Airline airline;

    @ManyToOne
    @JoinColumn(name = "origin_id", nullable = false)
    private Airport origin;

    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private Airport destination;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false)
    private BigDecimal basePrice; // Base price for ECONOMY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftType aircraftType; // e.g., BOEING_737, AIRBUS_A320. Useful for seat mapping.

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<seats> seats = new ArrayList<>();


    private int Size;


    @OneToMany(mappedBy = "flight")
    @ToString.Exclude
    private List<Booking> bookings = new ArrayList<>();
}