package com.example.Airline_Project.controller;

import com.example.Airline_Project.Repository.PaymentRepository;
import com.example.Airline_Project.Service.PaymentService;
import com.example.Airline_Project.model.PaymentOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Value("${paystack.webhook.secret}")
    private String paystackWebhookSecret;

    @Value("${flutterwave.webhook.secret}")
    private String flutterwaveWebhookSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/paystack")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {

        try {
            // Verify the signature
            if (!verifyPaystackSignature(payload, signature)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
            }

            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();
            JsonNode data = root.path("data");
            String reference = data.path("reference").asText();

            if ("charge.success".equals(event)) {
                // Find payment by reference
                PaymentOrder payment = paymentRepository.findByReference(reference)
                        .orElseThrow(() -> new Exception("Payment not found"));

                // Update payment status
                payment.setStatus(PaymentOrder.PaymentOrderStatus.SUCCESS);
                payment.setWebhookReference(generateWebhookReference());
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // TODO: Trigger any post-payment actions (e.g., send confirmation email)
            } else if ("refund.processed".equals(event)) {
                // Handle refund webhook
                PaymentOrder payment = paymentRepository.findByReference(reference)
                        .orElseThrow(() -> new Exception("Payment not found"));

                payment.setStatus(PaymentOrder.PaymentOrderStatus.REFUNDED);
                payment.setRefundReference(data.path("id").asText());
                payment.setWebhookReference(generateWebhookReference());
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing webhook: " + e.getMessage());
        }
    }

    @PostMapping("/flutterwave")
    public ResponseEntity<String> handleFlutterwaveWebhook(
            @RequestBody String payload,
            @RequestHeader("verif-hash") String signature) {

        try {
            // Verify the signature
            if (!verifyFlutterwaveSignature(payload, signature)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
            }

            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();
            JsonNode data = root.path("data");
            String reference = data.path("tx_ref").asText();

            if ("charge.completed".equals(event) && "successful".equals(data.path("status").asText())) {
                // Find payment by reference
                PaymentOrder payment = paymentRepository.findByReference(reference)
                        .orElseThrow(() -> new Exception("Payment not found"));

                // Update payment status
                payment.setStatus(PaymentOrder.PaymentOrderStatus.SUCCESS);
                payment.setWebhookReference(generateWebhookReference());
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // TODO: Trigger any post-payment actions
            } else if ("refund.completed".equals(event)) {
                // Handle refund webhook
                PaymentOrder payment = paymentRepository.findByReference(reference)
                        .orElseThrow(() -> new Exception("Payment not found"));

                payment.setStatus(PaymentOrder.PaymentOrderStatus.REFUNDED);
                payment.setRefundReference(data.path("id").asText());
                payment.setWebhookReference(generateWebhookReference());
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing webhook: " + e.getMessage());
        }
    }

    private boolean verifyPaystackSignature(String payload, String signature) {
        try {
            // Paystack uses HMAC SHA512 for signature verification
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    paystackWebhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            sha512Hmac.init(secretKey);

            // Compute the HMAC
            byte[] hash = sha512Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Encode to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // Compare the computed signature with the received signature
            return hexString.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifyFlutterwaveSignature(String payload, String signature) {
        try {
            // Flutterwave uses a simple hash comparison
            // The signature should match your Flutterwave webhook secret
            return flutterwaveWebhookSecret.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String generateWebhookReference() {
        return "WH_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }
}