package com.example.Airline_Project.Service;

import com.example.Airline_Project.Domain.VerificationType;
import com.example.Airline_Project.model.User;
import com.example.Airline_Project.model.VerificationCode;

public interface VerificationCodeService {
    VerificationCode sendVerificationCode(User user, VerificationType verificationType);

    VerificationCode getVericationCodeById(Long id) throws Exception;

    VerificationCode getVerificationCodeByUser(Long userId);


    void deleteVerificationCodeById(VerificationCode verificationCode);
}

