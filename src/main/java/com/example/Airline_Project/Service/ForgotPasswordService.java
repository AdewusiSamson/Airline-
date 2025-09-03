package com.example.Airline_Project.Service;

import com.example.Airline_Project.Domain.VerificationType;
import com.example.Airline_Project.model.ForgotPassswordToken;
import com.example.Airline_Project.model.User;

public interface ForgotPasswordService {
    ForgotPassswordToken createToken(User user, String id, String otp,
                                     VerificationType verificationType, String sendTo);


    ForgotPassswordToken findbyId(String id) throws Exception;

    ForgotPassswordToken findByUserId(Long userId) throws Exception;

    void deleteToken(ForgotPassswordToken forgotPassswordToken);
}
