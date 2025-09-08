package com.example.Airline_Project.Response;

import lombok.Data;

import java.util.List;
@Data
public class AIPredictionResponse {
    private Double predictedPrice;
    private Integer delayMinutes;
    private List<String> recommendations;
    private String weatherCondition;
    private Double confidenceScore;
    private String message;

}