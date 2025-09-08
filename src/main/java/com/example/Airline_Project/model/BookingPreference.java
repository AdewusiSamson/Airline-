package com.example.Airline_Project.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "booking_preferences")
public class BookingPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String seatPreference;
    private String mealPreference;
    private boolean travelInsurance;
    private int baggageAllowance  ; // in kg
    private String specialAssistance;}


