package com.example.Airline_Project.Service;

import com.example.Airline_Project.model.TwoFactorOTP;
import com.example.Airline_Project.model.User;

public interface TwoFactorotpService {

    TwoFactorOTP createTwoFactorOtp(User user, String otp, String jwt);


    TwoFactorOTP findByUser(Long userId);

    TwoFactorOTP findById(String id);


    boolean verifyTwoFactorOtp(TwoFactorOTP twoFactorOTP, String otp);

    void deleteTwoFactorOtp(TwoFactorOTP twoFactorOTP);


}