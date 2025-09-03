package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.seats;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface seatsRepository extends JpaRepository<seats, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.flight.id = :flightId AND s.seatNumber = :seatNumber")
    Optional<seats> findByFlightIdAndSeatNumberWithLock(
            @Param("flightId") Long flightId,
            @Param("seatNumber") String seatNumber
    );

    List<seats> findByStatusAndHeldAtBefore(seats.SeatStatus status, LocalDateTime heldAt);

    Optional<seats> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);
}

