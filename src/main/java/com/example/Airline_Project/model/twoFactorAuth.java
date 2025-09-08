package com.example.Airline_Project.model;

import com.example.Airline_Project.Domain.VerificationType;
import lombok.Data;

@Data
public class twoFactorAuth {
    private boolean isEnabled;
    private VerificationType sendTo;
}

