package com.example.Airline_Project.controller;

import com.example.Airline_Project.Domain.VerificationType;
import com.example.Airline_Project.OtpUtils;
import com.example.Airline_Project.Response.ApiResponse;
import com.example.Airline_Project.Response.AuthResponse;
import com.example.Airline_Project.Service.EmailService;
import com.example.Airline_Project.Service.ForgotPasswordService;
import com.example.Airline_Project.Service.UserService;
import com.example.Airline_Project.Service.VerificationCodeService;
import com.example.Airline_Project.model.ForgotPassswordToken;
import com.example.Airline_Project.model.User;
import com.example.Airline_Project.model.VerificationCode;
import com.example.Airline_Project.request.ForgotPasswordTokenRequest;
import com.example.Airline_Project.request.ResetPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ForgotPasswordService forgotPasswordService;
    @Autowired
    private VerificationCodeService verificationCodeService;

    @GetMapping("/api/users/profile")
    public ResponseEntity<User> getUserProfie(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/api/users/verifcation/{verificationType}/send-otp")
    public ResponseEntity<String> sendVerificationOtp(@RequestHeader("Authorization") String jwt, @PathVariable VerificationType verificationType) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        if (verificationCode == null) {
            VerificationCode verificationCode1 = verificationCodeService.sendVerificationCode(user, verificationType);
            if (verificationType == VerificationType.EMAIL) {
            }
            emailService.sendOtpEmail(user.getEmail(), verificationCode1.getOtp());
        }

        return new ResponseEntity<>("Verification otp sent successfully", HttpStatus.OK);
    }



    @PostMapping("/auth/users/reset-password/send-otp")
    public ResponseEntity<AuthResponse> forgotPasswordOtp(@RequestHeader("Authorization") String jwt, @PathVariable VerificationType verificationType, @RequestBody ForgotPasswordTokenRequest request) throws Exception {
        User user = userService.findUserByEmail(request.getSendTo());
        String otp = OtpUtils.generateOTP();
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();
        ForgotPassswordToken token = forgotPasswordService.findByUserId(user.getId());
        if (token == null) {

            token = forgotPasswordService.createToken(user, id, otp, request.getVerificationType(), request.getSendTo());
        }
        if (request.getVerificationType().equals(VerificationType.EMAIL)) {
            emailService.sendOtpEmail(user.getEmail(), token.getOtp());
        }
        AuthResponse response = new AuthResponse();
        response.setSession(token.getId());
        response.setMessage("otp sent successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PatchMapping("/api/users/enable-two-factor/verify-otp/{otp}")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String id,
            @RequestBody ResetPasswordRequest request,
            @RequestHeader("Authorization") String jwt) throws Exception {

        ForgotPassswordToken forgotPassswordToken = forgotPasswordService.findbyId(id);
        boolean isVerified = forgotPassswordToken.getOtp().equals(request.getOtp());
        if (isVerified) {
            userService.updatePassword(forgotPassswordToken.getUser(), request.getPassword());
            ApiResponse res = new ApiResponse();
            res.setMessage("Password updated successfully");
            return new ResponseEntity(res, HttpStatus.ACCEPTED);
        }
        throw new Exception("Invalid OTP");


    }
}