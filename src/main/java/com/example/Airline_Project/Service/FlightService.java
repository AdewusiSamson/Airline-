package com.example.Airline_Project.Service;

import com.example.Airline_Project.model.Flight;

import com.example.Airline_Project.model.Seat;


import java.time.LocalDate;
import java.util.List;

public interface FlightService {
    List<Flight>searchFlights(String originCode, String destinationCode, LocalDate date,String seatClass) throws Exception;
    Flight getFlightById(Long flightId)throws Exception;
    List<Seat>getSeatsForFlight(Long flightId)throws Exception;
}
