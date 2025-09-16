package com.example.Airline_Project.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class PaymentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long amount;

    private PaymentOrderStatus status;
    private PaymentMethod paymentMethod;
    private String reference;
    private String refundReference;
    private String webhookReference;
    private LocalDateTime webhookReceivedAt;
    private Integer webhookAttempts = 0;

    @ManyToOne
    private User user;
//
//    @OneToOne
//    @JoinColumn(name = "booking_id")
//    private Booking booking;

    public enum PaymentOrderStatus{
        PENDING,FAILED,SUCCESS,REFUNDED
    }
     public enum PaymentMethod{
            PAYSTACK,FLUTTERWAVE
     };
}

