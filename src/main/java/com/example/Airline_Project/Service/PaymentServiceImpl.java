package com.example.Airline_Project.Service;


import com.example.Airline_Project.Repository.PaymentRepository;
import com.example.Airline_Project.Response.PaymentResponse;
import com.example.Airline_Project.Service.PaymentService;
import com.example.Airline_Project.Service.UserService;
import com.example.Airline_Project.model.PaymentOrder;
import com.example.Airline_Project.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service

public class PaymentServiceImpl implements PaymentService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${paystack.api.secretkey}")
    private String paystackSecretKey;

    @Value("${flutterwave.api.secretKey}")
    private String flutterwaveSecretKey;

    @Value("${app.base.url}")
    private String appBaseUrl;

    @Value("${app.callback.url}")
    private String callbackUrl;

    @Value("${paystack.webhook.secret}")
    private String paystackWebhookSecret;

    @Value("${flutterwave.webhook.secret}")
    private String flutterwaveWebhookSecret;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public PaymentOrder createOrder(User user, Long amount, PaymentOrder.PaymentMethod paymentMethod) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setAmount(amount);
        paymentOrder.setUser(user);
        paymentOrder.setPaymentMethod(paymentMethod);
        paymentOrder.setStatus(PaymentOrder.PaymentOrderStatus.PENDING);

        // Generate a unique reference
        String reference = generateReference(paymentMethod);
        paymentOrder.setReference(reference);

        return paymentRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new Exception("Payment order not found"));
    }

    @Override
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws Exception {
        if (paymentOrder.getPaymentMethod().equals(PaymentOrder.PaymentMethod.PAYSTACK)) {
            return verifyPaystackPayment(paymentId);
        } else if (paymentOrder.getPaymentMethod().equals(PaymentOrder.PaymentMethod.FLUTTERWAVE)) {
            return verifyFlutterwavePayment(paymentId);
        }
        return false;
    }

    @Override
    public PaymentResponse createPaystackPaymentLink(User user, Long amount) throws Exception {
        try {
            // Create payment order first
            PaymentOrder paymentOrder = createOrder(user, amount, PaymentOrder.PaymentMethod.PAYSTACK);

            // Prepare request to Paystack
            String url = "https://api.paystack.co/transaction/initialize";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + paystackSecretKey);

            // Create request payload using the generated reference
            String requestJson = String.format(
                    "{\"email\": \"%s\", \"amount\": \"%d\", \"reference\": \"%s\", \"callback_url\": \"%s\"}",
                    user.getEmail(), amount * 100, paymentOrder.getReference(), callbackUrl
            );

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            boolean status = root.path("status").asBoolean();

            if (status) {
                JsonNode data = root.path("data");
                String paymentUrl = data.path("authorization_url").asText();

                // Create and return response
                PaymentResponse paymentResponse = new PaymentResponse();
                paymentResponse.setPayment_url(paymentUrl);
                paymentResponse.setRefrence(paymentOrder.getReference());

                return paymentResponse;
            } else {
                String message = root.path("message").asText();
                throw new Exception("Paystack error: " + message);
            }
        } catch (Exception e) {
            throw new Exception("Failed to create Paystack payment link: " + e.getMessage());
        }
    }

    @Override
    public void processWebhookEvent(String payload, String signature, String provider) throws Exception {

    }

    @Override
    public PaymentResponse createFlutterwavePaymentLink(User user, Long amount, Long orderId) throws Exception {
        try {
            // Get or create payment order
            PaymentOrder paymentOrder;
            if (orderId != null) {
                paymentOrder = getPaymentOrderById(orderId);
            } else {
                paymentOrder = createOrder(user, amount, PaymentOrder.PaymentMethod.FLUTTERWAVE);
            }

            // Prepare request to Flutterwave
            String url = "https://api.flutterwave.com/v3/payments";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + flutterwaveSecretKey);

            // Create request payload using the generated reference
            String requestJson = String.format(
                    "{\"tx_ref\": \"%s\", \"amount\": %d, \"currency\": \"NGN\", " +
                            "\"redirect_url\": \"%s/payment/callback\", " +
                            "\"callback_url\": \"%s\", " +
                            "\"customer\": {\"email\": \"%s\"}, " +
                            "\"customizations\": {\"title\": \"Airline Booking\", \"description\": \"Payment for flight booking\"}}",
                    paymentOrder.getReference(), amount, appBaseUrl, callbackUrl, user.getEmail()
            );

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText();

            if ("success".equals(status)) {
                JsonNode data = root.path("data");
                String paymentUrl = data.path("link").asText();

                // Create and return response
                PaymentResponse paymentResponse = new PaymentResponse();
                paymentResponse.setPayment_url(paymentUrl);
                paymentResponse.setRefrence(paymentOrder.getReference());

                return paymentResponse;
            } else {
                String message = root.path("message").asText();
                throw new Exception("Flutterwave error: " + message);
            }
        } catch (Exception e) {
            throw new Exception("Failed to create Flutterwave payment link: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponse confirmPayment(String reference, User user) throws Exception {
        Optional<PaymentOrder> confirmedPayment = paymentRepository.findByReference(reference);

        if (confirmedPayment.isPresent() && confirmedPayment.get().getStatus().equals(PaymentOrder.PaymentOrderStatus.SUCCESS)) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setPayment_url("Payment already confirmed");
            paymentResponse.setRefrence(reference);
            return paymentResponse;
        }

        // If payment is not confirmed, verify with payment gateway
        PaymentOrder payment = confirmedPayment.orElseThrow(() ->
                new Exception("Payment not found with reference: " + reference));

        // Verify the payment belongs to the user
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new Exception("You are not authorized to confirm this payment");
        }

        boolean verificationSuccess = false;

        if (payment.getPaymentMethod().equals(PaymentOrder.PaymentMethod.PAYSTACK)) {
            verificationSuccess = verifyPaystackPayment(reference);
        } else if (payment.getPaymentMethod().equals(PaymentOrder.PaymentMethod.FLUTTERWAVE)) {
            verificationSuccess = verifyFlutterwavePayment(reference);
        } else {
            throw new Exception("Unsupported payment method: " + payment.getPaymentMethod());
        }

        if (verificationSuccess) {
            payment.setStatus(PaymentOrder.PaymentOrderStatus.SUCCESS);
            paymentRepository.save(payment);

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setPayment_url("Payment confirmed successfully");
            paymentResponse.setRefrence(reference);
            return paymentResponse;
        } else {
            throw new Exception("Payment verification failed");
        }
    }

    @Override
    public PaymentResponse processRefund(String reference, User user) throws Exception {
        Optional<PaymentOrder> paymentOrderOpt = paymentRepository.findByReference(reference);

        if (paymentOrderOpt.isEmpty()) {
            throw new Exception("Payment not found with reference: " + reference);
        }

        PaymentOrder paymentOrder = paymentOrderOpt.get();

        // Verify the payment belongs to the user
        if (!paymentOrder.getUser().getId().equals(user.getId())) {
            throw new Exception("You are not authorized to refund this payment");
        }

        // Check if payment is eligible for refund
        if (!paymentOrder.getStatus().equals(PaymentOrder.PaymentOrderStatus.SUCCESS)) {
            throw new Exception("Only successful payments can be refunded");
        }

        if (paymentOrder.getStatus().equals(PaymentOrder.PaymentOrderStatus.REFUNDED)) {
            throw new Exception("Payment has already been refunded");
        }

        // Process refund based on payment method
        boolean refundSuccess = false;
        String refundReference = null;

        try {
            if (paymentOrder.getPaymentMethod().equals(PaymentOrder.PaymentMethod.PAYSTACK)) {
                refundSuccess = processPaystackRefund(reference);
                refundReference = "PSK_REF_" + System.currentTimeMillis();
            } else if (paymentOrder.getPaymentMethod().equals(PaymentOrder.PaymentMethod.FLUTTERWAVE)) {
                refundSuccess = processFlutterwaveRefund(reference);
                refundReference = "FLW_REF_" + System.currentTimeMillis();
            } else {
                throw new Exception("Unsupported payment method for refund");
            }

            if (refundSuccess) {
                // Update payment status to refunded
                paymentOrder.setStatus(PaymentOrder.PaymentOrderStatus.REFUNDED);
                paymentOrder.setRefundReference(refundReference);
                paymentRepository.save(paymentOrder);

                // Create and return response
                PaymentResponse paymentResponse = new PaymentResponse();
                paymentResponse.setPayment_url("Refund processed successfully");
                paymentResponse.setRefrence(reference);
                paymentResponse.setRefundReference(refundReference);

                return paymentResponse;
            } else {
                throw new Exception("Refund processing failed");
            }
        } catch (Exception e) {
            throw new Exception("Refund processing error: " + e.getMessage());
        }
    }

    // Webhook processing methods
    public boolean processPaystackWebhook(String payload, String signature) {
        try {
            // Verify signature
            if (!verifyPaystackSignature(payload, signature)) {
                return false;
            }

            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();
            JsonNode data = root.path("data");
            String reference = data.path("reference").asText();

            Optional<PaymentOrder> paymentOpt = paymentRepository.findByReference(reference);
            if (paymentOpt.isEmpty()) {
                return false;
            }

            PaymentOrder payment = paymentOpt.get();

            if ("charge.success".equals(event)) {
                payment.setStatus(PaymentOrder.PaymentOrderStatus.SUCCESS);
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return true;
            } else if ("refund.processed".equals(event)) {
                payment.setStatus(PaymentOrder.PaymentOrderStatus.REFUNDED);
                payment.setRefundReference(data.path("id").asText());
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean processFlutterwaveWebhook(String payload, String signature) {
        try {
            // Verify signature
            if (!verifyFlutterwaveSignature(payload, signature)) {
                return false;
            }

            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();
            String reference = root.path("data").path("tx_ref").asText();

            Optional<PaymentOrder> paymentOpt = paymentRepository.findByReference(reference);
            if (paymentOpt.isEmpty()) {
                return false;
            }

            PaymentOrder payment = paymentOpt.get();

            if ("charge.completed".equals(event) && "successful".equals(root.path("data").path("status").asText())) {
                payment.setStatus(PaymentOrder.PaymentOrderStatus.SUCCESS);
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return true;
            } else if ("refund.completed".equals(event)) {
                payment.setStatus(PaymentOrder.PaymentOrderStatus.REFUNDED);
                payment.setRefundReference(root.path("data").path("id").asText());
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper methods
    private String generateReference(PaymentOrder.PaymentMethod paymentMethod) {
        String prefix = paymentMethod.equals(PaymentOrder.PaymentMethod.PAYSTACK) ? "paystack_" : "flutterwave_";
        return prefix + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    private Boolean verifyPaystackPayment(String reference) throws Exception {
        try {
            String url = "https://api.paystack.co/transaction/verify/" + reference;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + paystackSecretKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            boolean status = root.path("status").asBoolean();

            if (status) {
                JsonNode data = root.path("data");
                String paymentStatus = data.path("status").asText();

                // Update the payment order status if needed
                if ("success".equals(paymentStatus)) {
                    PaymentOrder order = paymentRepository.findByReference(reference)
                            .orElseThrow(() -> new Exception("Payment order not found"));
                    order.setStatus(PaymentOrder.PaymentOrderStatus.SUCCESS);
                    paymentRepository.save(order);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            throw new Exception("Failed to verify Paystack payment: " + e.getMessage());
        }
    }

    private Boolean verifyFlutterwavePayment(String reference) throws Exception {
        try {
            // For Flutterwave, we need to use the transaction ID for verification
            // First, we might need to get the transaction ID using the reference
            String url = "https://api.flutterwave.com/v3/transactions/verify_by_reference?tx_ref=" + reference;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + flutterwaveSecretKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText();

            if ("success".equals(status)) {
                JsonNode data = root.path("data");
                String paymentStatus = data.path(0).path("status").asText();

                // Update the payment order status if needed
                if ("successful".equals(paymentStatus)) {
                    PaymentOrder order = paymentRepository.findByReference(reference)
                            .orElseThrow(() -> new Exception("Payment order not found"));
                    order.setStatus(PaymentOrder.PaymentOrderStatus.SUCCESS);
                    paymentRepository.save(order);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            throw new Exception("Failed to verify Flutterwave payment: " + e.getMessage());
        }
    }

    private boolean processPaystackRefund(String reference) throws Exception {
        try {
            String url = "https://api.paystack.co/refund";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + paystackSecretKey);

            String requestJson = String.format("{\"transaction\": \"%s\"}", reference);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            boolean status = root.path("status").asBoolean();

            return status && root.path("data").path("status").asText().equals("processed");
        } catch (Exception e) {
            throw new Exception("Paystack refund failed: " + e.getMessage());
        }
    }

    private boolean processFlutterwaveRefund(String reference) throws Exception {
        try {
            String url = "https://api.flutterwave.com/v3/transactions/" + reference + "/refund";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + flutterwaveSecretKey);

            HttpEntity<String> entity = new HttpEntity<>("", headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText();

            return "success".equals(status) && root.path("data").path("status").asText().equals("successful");
        } catch (Exception e) {
            throw new Exception("Flutterwave refund failed: " + e.getMessage());
        }
    }

    private boolean verifyPaystackSignature(String payload, String signature) {
        try {
            // Paystack uses HMAC SHA512 for signature verification
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(paystackWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(secretKey);

            byte[] hash = sha512Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getEncoder().encodeToString(hash);

            return computedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifyFlutterwaveSignature(String payload, String signature) {
        // Flutterwave uses a simple hash comparison
        return flutterwaveWebhookSecret.equals(signature);
    }
}