package com.example.Airline_Project.controller;

import com.example.Airline_Project.Service.BookingService;
import com.example.Airline_Project.Service.UserService;
import com.example.Airline_Project.model.Booking;
import com.example.Airline_Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;
@Autowired
    private UserService userService;
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestParam Long flightId,
            @RequestParam String seatNumber,
            @RequestParam String passengerFirstName,
            @RequestParam String passengerLastName,
            @RequestHeader("Authorization") String jwt) {

        try {
            // Extract user from JWT (you'll need to implement this)
            User user = userService.findUserProfileByJwt(jwt);

            Booking booking = bookingService.createBooking(
                    flightId, seatNumber, passengerFirstName, passengerLastName, user);

            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getUserBookings(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserProfileByJwt(jwt);
            List<Booking> bookings = bookingService.getUserBookings(user);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{pnr}")
    public ResponseEntity<Booking> getBookingByPnr(@PathVariable String pnr) {
        try {
            Booking booking = bookingService.getBookingByPnr(pnr);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


}