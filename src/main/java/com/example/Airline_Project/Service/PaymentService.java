package com.example.Airline_Project.Service;

import com.example.Airline_Project.Response.PaymentResponse;
import com.example.Airline_Project.model.PaymentOrder;
import com.example.Airline_Project.model.User;

public interface PaymentService {
    PaymentOrder createOrder(User user, Long amount, PaymentOrder.PaymentMethod paymentMethod);

    PaymentOrder getPaymentOrderById(Long id) throws Exception;

    Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws Exception;

    PaymentResponse createPaystackPaymentLink(User user, Long amount) throws Exception;


    void processWebhookEvent(String payload, String signature, String provider) throws Exception;

    PaymentResponse createFlutterwavePaymentLink(User user, Long amount, Long orderId) throws Exception;

    PaymentResponse confirmPayment(String reference, User user) throws Exception;

    PaymentResponse processRefund(String reference, User user) throws Exception;
}
