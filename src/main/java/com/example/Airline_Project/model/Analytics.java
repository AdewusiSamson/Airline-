package com.example.Airline_Project.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;


@Data
@Entity
@Table(name = "analytics")
public class Analytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private int totalBookings;
    private double totalRevenue;
    private int newUsers;
    private int flightsTaken;
    private String mostPopularRoute;

}
