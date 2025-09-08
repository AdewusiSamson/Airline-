package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.TwoFactorOTP;
import com.example.Airline_Project.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwoFactorOtpRepostiory extends JpaRepository<TwoFactorOTP, String> {
    TwoFactorOTP findByUserId(long userId);
}
