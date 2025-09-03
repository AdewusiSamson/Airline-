package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.ForgotPassswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassswordToken, String> {

    ForgotPassswordToken findByUserId(Long userId);
}
