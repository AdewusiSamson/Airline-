package com.example.Airline_Project.Service;

import com.example.Airline_Project.Response.AIPredictionResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AIServiceImpl implements AIService {

    @Override
    public AIPredictionResponse predictFlightPrice(String origin, String destination, String departureDate, String bookingDate) {
        // This would integrate with your Python AI service
        // For now, we'll return mock data

        double basePrice = 300.0;
        Random random = new Random();

        // Simple prediction algorithm (would be replaced with actual AI model)
        double predictedPrice = basePrice;

        // Distance factor (mock calculation)
        Map<String, Integer> distanceMap = new HashMap<>();
        distanceMap.put("JFK-LAX", 2500);
        distanceMap.put("LHR-DXB", 3400);
        distanceMap.put("DXB-SIN", 3800);
        // Add more routes as needed

        String route = origin + "-" + destination;
        if (distanceMap.containsKey(route)) {
            predictedPrice += distanceMap.get(route) * 0.15;
        }

        // Time-based pricing (closer to departure = more expensive)
        try {
            LocalDate depDate = LocalDate.parse(departureDate);
            LocalDate bookDate = LocalDate.parse(bookingDate);
            long daysUntilDeparture = java.time.temporal.ChronoUnit.DAYS.between(bookDate, depDate);

            if (daysUntilDeparture < 7) {
                predictedPrice *= 1.5; // 50% increase for last-minute bookings
            } else if (daysUntilDeparture < 30) {
                predictedPrice *= 1.2; // 20% increase for bookings within 30 days
            }
        } catch (Exception e) {
            // Use default pricing if date parsing fails
        }

        // Add some random variation
        double variation = (random.nextDouble() * 0.4) - 0.2; // ±20% variation
        predictedPrice *= (1 + variation);

        AIPredictionResponse response = new AIPredictionResponse();
        response.setPredictedPrice(predictedPrice);
        response.setConfidenceScore(0.85);
        response.setMessage("Price prediction based on historical data and current demand");

        return response;
    }

    @Override
    public AIPredictionResponse predictFlightDelay(String flightNumber) {
        // Mock delay prediction - would integrate with actual AI service
        Random random = new Random();
        int delayMinutes = random.nextInt(120); // 0-120 minutes delay

        AIPredictionResponse response = new AIPredictionResponse();
        response.setDelayMinutes(delayMinutes);
        response.setConfidenceScore(0.75);

        if (delayMinutes > 60) {
            response.setMessage("High probability of significant delay");
        } else if (delayMinutes > 30) {
            response.setMessage("Moderate probability of delay");
        } else {
            response.setMessage("Low probability of delay");
        }

        return response;
    }

    @Override
    public AIPredictionResponse getPersonalizedRecommendations(Long userId) {
        // Mock recommendations - would integrate with actual AI service
        List<String> recommendations = Arrays.asList(
                "Bali, Indonesia - Beach getaway",
                "Paris, France - Romantic destination",
                "Tokyo, Japan - Cultural experience",
                "New York, USA - City adventure"
        );

        AIPredictionResponse response = new AIPredictionResponse();
        response.setRecommendations(recommendations);
        response.setConfidenceScore(0.80);
        response.setMessage("Personalized recommendations based on your travel history");

        return response;
    }

    @Override
    public AIPredictionResponse getWeatherForecast(String airportCode) {
        // Mock weather forecast - would integrate with actual weather API
        String[] weatherConditions = {"Sunny", "Cloudy", "Rainy", "Stormy"};
        Random random = new Random();
        String weather = weatherConditions[random.nextInt(weatherConditions.length)];

        AIPredictionResponse response = new AIPredictionResponse();
        response.setWeatherCondition(weather);
        response.setConfidenceScore(0.90);
        response.setMessage("Weather forecast for " + airportCode);

        return response;
}
}
