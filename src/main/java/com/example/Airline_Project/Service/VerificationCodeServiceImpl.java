package com.example.Airline_Project.Service;

import com.example.Airline_Project.Domain.VerificationType;
import com.example.Airline_Project.OtpUtils;
import com.example.Airline_Project.Repository.VerificationCodeRepository;
import com.example.Airline_Project.model.User;
import com.example.Airline_Project.model.VerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepositiory;

    @Override
    public VerificationCode sendVerificationCode(User user, VerificationType verificationType) {
        VerificationCode verificationCode1 = new VerificationCode();
        verificationCode1.setOtp(OtpUtils.generateOTP());
        verificationCode1.setVerificationType(verificationType);
        verificationCode1.setUser(user);


        return verificationCodeRepositiory.save(verificationCode1);
    }

    @Override
    public VerificationCode getVericationCodeById(Long id) throws Exception {
        Optional<VerificationCode> verificationCode = verificationCodeRepositiory.findById(id);
        if (verificationCode.isPresent()) {
            return verificationCode.get();
        }

        throw new Exception("Verification code not found ");
    }

    @Override
    public VerificationCode getVerificationCodeByUser(Long userId) {
        return verificationCodeRepositiory.findByUserId(userId);
    }

    @Override
    public void deleteVerificationCodeById(VerificationCode verificationCode) {
        verificationCodeRepositiory.delete(verificationCode);
    }
}
