package com.example.Airline_Project.Service;


import com.example.Airline_Project.model.Booking;
import com.example.Airline_Project.model.User;

import java.util.List;

public interface BookingService {
    Booking createBooking(Long flightId, String seatNumber, String passengerFirstName,String passengerLastName, User user);
    List<Booking> getUserBookings(User user);
    Booking getBookingByPnr(String pnr) throws Exception;
}