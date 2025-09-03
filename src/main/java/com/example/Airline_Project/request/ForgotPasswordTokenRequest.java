package com.example.Airline_Project.request;

import com.example.Airline_Project.Domain.VerificationType;
import lombok.Data;

@Data
public class ForgotPasswordTokenRequest {
    private String SendTo;
    private VerificationType verificationType;

}

