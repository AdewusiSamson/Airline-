package com.example.Airline_Project.Response;

import lombok.Data;

@Data
public class PaymentResponse {
    private String payment_url;
    private String refrence;
    private String refundReference;
}
