package com.example.Airline_Project.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String Otp;
    private String password;

}

