package com.example.Airline_Project.Service;



import com.example.Airline_Project.Repository.seatsRepository;
import com.example.Airline_Project.model.Seat;

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
    public Seat holdSeat(Long flightId, String seatNumber, String userEmail) {
        // Find the seat with pessimistic locking to prevent concurrent access
        Seat seat = seatRepository.findByFlightIdAndSeatNumberWithLock(flightId, seatNumber)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));

        // Check if seat is available or has an expired hold
        if (seat.getStatus() == Seat.SeatStatus.AVAILABLE ||
                (seat.getStatus() == Seat.SeatStatus.HELD &&
                        seat.getHeldAt().isBefore(LocalDateTime.now().minusMinutes(10)))) {

            // Update seat status and hold information
            seat.setStatus(Seat.SeatStatus.HELD);
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
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));

        if (seat.getStatus() == Seat.SeatStatus.HELD) {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldAt(null);
            seat.setHeldBy(null);
            seatRepository.save(seat);
        }
    }

    @Override
    @Transactional
    public void confirmSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));

        if (seat.getStatus() == Seat.SeatStatus.HELD) {
            seat.setStatus(Seat.SeatStatus.BOOKED);
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
        List<Seat> expiredSeats = seatRepository.findByStatusAndHeldAtBefore(
                Seat.SeatStatus.HELD, tenMinutesAgo);

        for (Seat seat : expiredSeats) {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldAt(null);
            seat.setHeldBy(null);
        }

        seatRepository.saveAll(expiredSeats);
    }
}
