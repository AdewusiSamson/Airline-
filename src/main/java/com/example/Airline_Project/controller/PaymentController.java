package com.example.Airline_Project.controller;


import com.example.Airline_Project.Response.PaymentResponse;
import com.example.Airline_Project.Service.PaymentService;
import com.example.Airline_Project.Service.UserService;
import com.example.Airline_Project.model.PaymentOrder;
import com.example.Airline_Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {
    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment/{paymentMethod}/amount/{amount}")
    public ResponseEntity<PaymentResponse> paymentHandler(
            @PathVariable PaymentOrder.PaymentMethod paymentMethod,
            @PathVariable Long amount,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);
        PaymentResponse paymentResponse;

        if (paymentMethod.equals(PaymentOrder.PaymentMethod.PAYSTACK)) {
            paymentResponse = paymentService.createPaystackPaymentLink(user, amount);
            return ResponseEntity.ok(paymentResponse);
        } else if (paymentMethod.equals(PaymentOrder.PaymentMethod.FLUTTERWAVE)) {

            PaymentOrder order = paymentService.createOrder(user, amount, paymentMethod);
            paymentResponse = paymentService.createFlutterwavePaymentLink(user, amount, order.getId());
            return ResponseEntity.ok(paymentResponse);
        } else {
            throw new Exception("Unsupported payment method");
        }
    }

}
