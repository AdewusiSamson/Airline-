package com.example.Airline_Project.controller;


import com.example.Airline_Project.Service.SeatService;
import com.example.Airline_Project.configuration.JwtProvider;
import com.example.Airline_Project.model.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;
    @Autowired
     private JwtProvider jwtProvider;

    @PostMapping("/hold")
    public ResponseEntity<Seat> holdSeat(
            @RequestParam Long flightId,
            @RequestParam String seatNumber,
            @RequestHeader("Authorization") String jwt) {

        try {
            // Extract user email from JWT
            String userEmail = JwtProvider.getEmailFromToken(jwt);

            Seat seat = seatService.holdSeat(flightId, seatNumber, userEmail);
            return ResponseEntity.ok(seat);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{seatId}/release")
    public ResponseEntity<Void> releaseSeat(@PathVariable Long seatId) {
        try {
            seatService.releaseSeat(seatId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{seatId}/confirm")
    public ResponseEntity<Void> confirmSeat(@PathVariable Long seatId) {
        try {
            seatService.confirmSeat(seatId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    }
