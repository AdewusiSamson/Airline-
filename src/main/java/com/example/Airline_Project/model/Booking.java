package com.example.Airline_Project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String pnr; // PNR: 6-character unique code, e.g., "ABC123". Generate this on creation.

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(nullable = false)
    private String passengerFirstName;

    @Column(nullable = false)
    private String passengerLastName;


    // We denormalize this for data integrity. The seat might change status, but this booking is for *this* seat.
    @Column(nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status; // CONFIRMED, CANCELLED

    private BigDecimal totalPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @OneToOne
   private PaymentOrder payment;

    private double TotalAmount;
    private LocalDateTime bookingDate;

    public enum BookingStatus {
        CONFIRMED, CANCELLED
    }
}