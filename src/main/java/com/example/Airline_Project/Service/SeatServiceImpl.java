package com.example.Airline_Project.Service;



import com.example.Airline_Project.Repository.seatsRepository;
import com.example.Airline_Project.model.seats;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final seatsRepository seatRepository;

    @Override
    @Transactional
    public seats holdSeat(Long flightId, String seatNumber, String userEmail) {
        // Find the seat with pessimistic locking to prevent concurrent access
        seats seat = seatRepository.findByFlightIdAndSeatNumberWithLock(flightId, seatNumber)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));

        // Check if seat is available or has an expired hold
        if (seat.getStatus() == seats.SeatStatus.AVAILABLE ||
                (seat.getStatus() == seats.SeatStatus.HELD &&
                        seat.getHeldAt().isBefore(LocalDateTime.now().minusMinutes(10)))) {

            // Update seat status and hold information
            seat.setStatus(seats.SeatStatus.HELD);
            seat.setHeldAt(LocalDateTime.now());
            seat.setHeldBy(userEmail);

            return seatRepository.save(seat);
        } else {
            throw new IllegalStateException("Seat is not available");
        }
    }

    @Override
    @Transactional
    public void releaseSeat(Long seatId) {
        seats seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));

        if (seat.getStatus() == seats.SeatStatus.HELD) {
            seat.setStatus(seats.SeatStatus.AVAILABLE);
            seat.setHeldAt(null);
            seat.setHeldBy(null);
            seatRepository.save(seat);
        }
    }

    @Override
    @Transactional
    public void confirmSeat(Long seatId) {
        seats seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));

        if (seat.getStatus() == seats.SeatStatus.HELD) {
            seat.setStatus(seats.SeatStatus.BOOKED);
            seatRepository.save(seat);
        } else {
            throw new IllegalStateException("Seat is not in HELD status");
        }
    }

    @Override
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void releaseExpiredHolds() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<seats> expiredSeats = seatRepository.findByStatusAndHeldAtBefore(
                seats.SeatStatus.HELD, tenMinutesAgo);

        for (seats seat : expiredSeats) {
            seat.setStatus(seats.SeatStatus.AVAILABLE);
            seat.setHeldAt(null);
            seat.setHeldBy(null);
        }

        seatRepository.saveAll(expiredSeats);
    }
}
