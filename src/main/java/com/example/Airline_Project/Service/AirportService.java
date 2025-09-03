package com.example.Airline_Project.Service;


import com.example.Airline_Project.model.Airport;

import java.util.List;

public interface AirportService {
    List<Airport> getAllAirports();
    Airport getAirportByCode(String code) throws Exception;
}
