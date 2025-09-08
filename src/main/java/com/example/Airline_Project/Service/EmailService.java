package com.example.Airline_Project.Service;

import java.io.UnsupportedEncodingException;

public interface EmailService {
    void sendWelcomeEmail(String toEmail, String subscriberName, String unsubscribeToken) throws UnsupportedEncodingException;
    void sendFirstFollowupEmail(String toEmail, String subscriberName) throws UnsupportedEncodingException;
    void sendMonthlyNewsletter(String toEmail, String subscriberName) throws UnsupportedEncodingException;
    void sendBirthdayEmail(String toEmail, String subscriberName) throws UnsupportedEncodingException;
    void sendReengagementEmail(String toEmail, String subscriberName) throws UnsupportedEncodingException;
    void sendOtpEmail(String toEmail, String otpCode) throws UnsupportedEncodingException;
    void sendBookingConfirmation(String toEmail, String passengerName, String bookingReference) throws UnsupportedEncodingException;
    void sendPasswordResetEmail(String toEmail, String resetToken) throws UnsupportedEncodingException;
    void sendGenericEmail(String toEmail, String subject, String htmlContent) throws UnsupportedEncodingException;
}
