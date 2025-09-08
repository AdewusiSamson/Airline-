package com.example.Airline_Project.controller;


import com.example.Airline_Project.Response.PaymentResponse;
import com.example.Airline_Project.Service.PaymentService;
import com.example.Airline_Project.Service.UserService;
import com.example.Airline_Project.model.PaymentOrder;
import com.example.Airline_Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @RequestParam String reference,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserProfileByJwt(jwt);
            // Verify payment with Paystack/Flutterwave
            PaymentResponse response = paymentService.confirmPayment(reference, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> processRefund(
            @RequestParam String paymentReference,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserProfileByJwt(jwt);
            PaymentResponse response = paymentService.processRefund(paymentReference, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    //TODO webhook for payment verification
    //TODO create service for processing refunds and payment verifications

}
