package com.example.Airline_Project.Service;

import com.example.Airline_Project.model.seats;

public interface SeatService {

    seats holdSeat(Long flightId, String seatNumber, String userEmail);
    void releaseSeat(Long seatId);

    void confirmSeat(Long seatId);



    void releaseExpiredHolds();
}
