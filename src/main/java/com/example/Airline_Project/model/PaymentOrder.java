package com.example.Airline_Project.model;

import jakarta.persistence.*;
import lombok.Data;

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

    @ManyToOne
    private User user;

    public enum PaymentOrderStatus{
        PENDING,FAILED,SUCCESS
    }
     public enum PaymentMethod{
            PAYSTACK,FLUTTERWAVE
     };
}

