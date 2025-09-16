package com.example.Airline_Project.Service;

import com.example.Airline_Project.configuration.emailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private emailConfig emailConfig;

    @Autowired
    private TemplateEngine templateEngine;

    @Async
    @Override
    public void sendWelcomeEmail(String toEmail, String subscriberName, String unsubscribeToken) throws UnsupportedEncodingException {
        String subject = "Welcome Aboard! Your Journey with Emirates Airlines Begins Here ✈";

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", subscriberName);
        variables.put("unsubscribeToken", unsubscribeToken);
        variables.put("websiteUrl", "https://yourairlinewebsite.com");

        String htmlContent = generateEmailContent("welcome-template", variables);
        sendGenericEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendFirstFollowupEmail(String toEmail, String subscriberName) throws UnsupportedEncodingException {
        String subject = "Your First Exclusive Offer Inside! 🎁";

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", subscriberName);
        variables.put("discountCode", "WELCOME15");

        String htmlContent = generateEmailContent("followup-template", variables);
        sendGenericEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendMonthlyNewsletter(String toEmail, String subscriberName) throws UnsupportedEncodingException {
        String subject = "✈ This Month's Top Destinations & Special Offers";

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", subscriberName);
        // Add current month's deals and destinations

        String htmlContent = generateEmailContent("newsletter-template", variables);
        sendGenericEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendBirthdayEmail(String toEmail, String subscriberName) throws UnsupportedEncodingException {
        String subject = "Happy Birthday! A Special Gift from Emirates Airlines 🎂";

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", subscriberName);
        variables.put("discountCode", "BDAY20");

        String htmlContent = generateEmailContent("birthday-template", variables);
        sendGenericEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendReengagementEmail(String toEmail, String subscriberName) throws UnsupportedEncodingException {
        String subject = "We Miss You! Here's a Special Offer to Welcome You Back ✈";

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", subscriberName);
        variables.put("discountCode", "COMEBACK25");

        String htmlContent = generateEmailContent("reengagement-template", variables);
        sendGenericEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendOtpEmail(String toEmail, String otpCode) throws UnsupportedEncodingException {
        String subject = "Your Verification Code - Emirates Airlines";

        Map<String, Object> variables = new HashMap<>();
        variables.put("otp", otpCode);

        String htmlContent = generateEmailContent("otp-template", variables);
        sendGenericEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendBookingConfirmation(String toEmail, String passengerName, String bookingReference) throws UnsupportedEncodingException {
        String subject = "Booking Confirmation - " + bookingReference;

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", passengerName);
        variables.put("bookingReference", bookingReference);

        String htmlContent = generateEmailContent("booking-confirmation-template", variables);
        sendGenericEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) throws UnsupportedEncodingException {
        String subject = "Password Reset Request - Emirates Airlines";

        Map<String, Object> variables = new HashMap<>();
        variables.put("resetToken", resetToken);
        variables.put("resetUrl", "https://yourairlinewebsite.com/reset-password?token=" + resetToken);

        String htmlContent = generateEmailContent("password-reset-template", variables);
        sendGenericEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendGenericEmail(String toEmail, String subject, String htmlContent) throws UnsupportedEncodingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFromEmail(), emailConfig.getFromName());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String generateEmailContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process("emails/" + templateName, context);
    }
}

