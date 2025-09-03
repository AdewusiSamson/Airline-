package com.example.Airline_Project.model;

import com.example.Airline_Project.Domain.VerificationType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ForgotPassswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    @OneToOne
    private User user;

    private String otp;

    private VerificationType verificationType;

    private String sendTo;

}

