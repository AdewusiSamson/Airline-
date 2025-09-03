package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.AirportRepository;
import com.example.Airline_Project.Repository.FlightRepository;
import com.example.Airline_Project.model.Airport;
import com.example.Airline_Project.model.Flight;

import com.example.Airline_Project.model.seats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlightServiceImpl implements FlightService {

    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Override
    public List<Flight> searchFlights(String originCode, String destinationCode, LocalDate date, String seatClass) throws Exception {
        Airport origin = airportRepository.findByCode(originCode)
                .orElseThrow(()->new Exception(" airport not found: " + originCode));
        Airport destination = airportRepository.findByCode(destinationCode)
                .orElseThrow(() -> new Exception(" airport not found: " + destinationCode));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Flight> flights = flightRepository.findByOriginAndDestinationAndDepartureTimeBetween(
                origin, destination, startOfDay, endOfDay);

        // 4. Filter flights that have available seats in the requested class
        return flights.stream()
                .filter(flight -> flight.getSeats().stream()
                        .anyMatch(seat -> seat.getSeatClass().name().equals(seatClass) &&
                                seat.getStatus() == seats.SeatStatus.AVAILABLE)
                )
                .collect(Collectors.toList());
    }



    @Override
    public Flight getFlightById(Long flightId) throws Exception {
             Flight  flight =flightRepository.findByFlightById(flightId);
                if(flight==null){
                    throw new Exception("Flight not found with id: " + flightId);
                }
                return flight;
    }

    @Override
    public List<seats> getSeatsForFlight(Long flightId) throws Exception {
        Flight flight = getFlightById(flightId);
        return flight.getSeats();
    }
}