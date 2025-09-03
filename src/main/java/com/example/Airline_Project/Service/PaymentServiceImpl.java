package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.PaymentRepository;
import com.example.Airline_Project.Response.PaymentResponse;
import com.example.Airline_Project.model.PaymentOrder;
import com.example.Airline_Project.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
                            "\"customizations\": {\"title\": \"Your App Name\", \"description\": \"Payment for services\"}}",
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

    // Helper method to generate unique references
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
}
