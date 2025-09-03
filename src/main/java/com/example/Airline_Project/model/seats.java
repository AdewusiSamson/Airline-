package com.example.Airline_Project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
    @Entity
    @Table(name = "seats", uniqueConstraints = {
            @UniqueConstraint(columnNames = {"flight_id", "seat_number"})
    })
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class seats {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "flight_id", nullable = false)
        private Flight flight;

        @Column(name = "seat_number", nullable = false) // e.g., "12A"
        private String seatNumber;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private SeatClass seatClass; // ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private SeatStatus status = SeatStatus.AVAILABLE; // AVAILABLE, HELD, BOOKED

        private BigDecimal price; // Can be different from base flight price (e.g., for exit rows)

        private LocalDateTime heldAt; // Timestamp for when the seat was put on hold
        private String heldBy; // Could be a session ID or user ID. We'll use this for TTL.


        public enum SeatStatus {
            AVAILABLE, HELD, BOOKED
        }

        public enum SeatClass {
            ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST
        }
    }

