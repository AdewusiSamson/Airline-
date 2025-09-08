package com.example.Airline_Project.Service;


import com.example.Airline_Project.Response.AIPredictionResponse;

public interface AIService {
    AIPredictionResponse predictFlightPrice(String origin, String destination, String departureDate, String bookingDate);
    AIPredictionResponse predictFlightDelay(String flightNumber);
    AIPredictionResponse getPersonalizedRecommendations(Long userId);
    AIPredictionResponse getWeatherForecast(String airportCode);
}
