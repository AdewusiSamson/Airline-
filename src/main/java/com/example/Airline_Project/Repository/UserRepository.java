package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    int countByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
