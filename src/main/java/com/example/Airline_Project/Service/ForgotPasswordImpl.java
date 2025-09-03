package com.example.Airline_Project.Service;

import com.example.Airline_Project.Domain.VerificationType;
import com.example.Airline_Project.Repository.ForgotPasswordRepository;
import com.example.Airline_Project.model.ForgotPassswordToken;
import com.example.Airline_Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class ForgotPasswordImpl implements ForgotPasswordService {
    @Autowired
    private ForgotPasswordRepository passwordRepository;

    @Override
    public ForgotPassswordToken createToken(User user, String id, String otp, VerificationType verificationType, String sendTo) {
        ForgotPassswordToken Token = new ForgotPassswordToken();
        Token.setUser(user);
        Token.setSendTo(sendTo);
        Token.setOtp(otp);
        Token.setId(id);


        return passwordRepository.save(Token);
    }

    @Override
    public ForgotPassswordToken findbyId(String id) throws Exception {
        Optional<ForgotPassswordToken> token = passwordRepository.findById(id);
        return token.orElse(null);
    }

    @Override
    public ForgotPassswordToken findByUserId(Long userId) throws Exception {

        return passwordRepository.findByUserId(userId);
    }

    @Override
    public void deleteToken(ForgotPassswordToken token) {
        passwordRepository.delete(token);

    }
}

