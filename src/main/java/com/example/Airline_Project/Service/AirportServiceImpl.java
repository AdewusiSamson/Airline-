package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.AirportRepository;
import com.example.Airline_Project.model.Airport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class AirportServiceImpl implements AirportService{
 @Autowired
  private   AirportRepository airportRepository;;
    @Override
    public List<Airport> getAllAirports() {
        return airportRepository.findAll();
    }

    @Override
    public Airport getAirportByCode(String code) throws Exception {
        Optional<Airport> airport= airportRepository.findByCode(code);
        if(airport.isPresent()){
            return airport.get();
        }
        throw new Exception("Airport not found");
    }

}
