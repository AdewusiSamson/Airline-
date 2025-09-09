package com.example.Airline_Project.controller;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/paystack")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {

        try {
            // Verify the signature (implement this method)
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
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing webhook");
        }
    }

    @PostMapping("/flutterwave")
    public ResponseEntity<String> handleFlutterwaveWebhook(
            @RequestBody String payload,
            @RequestHeader("verif-hash") String signature) {

        try {
            // Verify the signature (implement this method)
            if (!verifyFlutterwaveSignature(payload, signature)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
            }

            JsonNode root = objectMapper.readTree(payload);
            String status = root.path("status").asText();
            String reference = root.path("tx_ref").asText();

            if ("successful".equals(status)) {
                // Find payment by reference
                PaymentOrder payment = paymentRepository.findByReference(reference)
                        .orElseThrow(() -> new Exception("Payment not found"));

                // Update payment status
                payment.setStatus(PaymentOrder.PaymentOrderStatus.SUCCESS);
                payment.setWebhookReference(generateWebhookReference());
                payment.setWebhookReceivedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // TODO: Trigger any post-payment actions
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing webhook");
        }
    }

    private boolean verifyPaystackSignature(String payload, String signature) {
        // Implement Paystack signature verification
        // This should use HMAC SHA512 with your Paystack secret key
        // Refer to Paystack documentation for exact implementation
        return true; // Placeholder
    }

    private boolean verifyFlutterwaveSignature(String payload, String signature) {
        // Implement Flutterwave signature verification
        // This should compare the signature with your Flutterwave webhook secret
        // Refer to Flutterwave documentation for exact implementation
        return true; // Placeholder
    }

    private String generateWebhookReference() {
        return "WH_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }
}