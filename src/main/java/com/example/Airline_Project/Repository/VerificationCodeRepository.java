package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode , Long> {
    VerificationCode findByUserId(Long userId);
}
