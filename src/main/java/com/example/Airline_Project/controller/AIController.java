package com.example.Airline_Project.controller;

import com.example.Airline_Project.Response.AIPredictionResponse;
import com.example.Airline_Project.Service.AIService;
import com.example.Airline_Project.request.AIPredictionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/price-prediction")
    public ResponseEntity<AIPredictionResponse> predictFlightPrice(@RequestBody AIPredictionRequest request) {
        AIPredictionResponse response = aiService.predictFlightPrice(
                request.getOrigin(),
                request.getDestination(),
                request.getDepartureDate(),
                request.getBookingDate()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/delay-prediction/{flightNumber}")
    public ResponseEntity<AIPredictionResponse> predictFlightDelay(@PathVariable String flightNumber) {
        AIPredictionResponse response = aiService.predictFlightDelay(flightNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<AIPredictionResponse> getPersonalizedRecommendations(@PathVariable Long userId) {
        AIPredictionResponse response = aiService.getPersonalizedRecommendations(userId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/weather-forecast/{airportCode}")
    public ResponseEntity<AIPredictionResponse> getWeatherForecast(@PathVariable String airportCode) {
        AIPredictionResponse response = aiService.getWeatherForecast(airportCode);
        return ResponseEntity.ok(response);
}
}
