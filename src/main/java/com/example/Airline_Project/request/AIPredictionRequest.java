package com.example.Airline_Project.request;

import lombok.Data;

@Data
public class AIPredictionRequest {
    private String origin;
    private String destination;
    private String departureDate;
    private String bookingDate;

}