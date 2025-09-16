package com.example.Airline_Project.Service;

import com.example.Airline_Project.Domain.VerificationType;
import com.example.Airline_Project.model.User;

public interface UserService {

    User findUserProfileByJwt(String jwt) throws Exception;

    User findUserByEmail(String email) throws Exception;

    User findUserById(Long userid) throws Exception;

    User updatePassword(User user, String newPassword);
    User enableTwoFactorAuthentication(VerificationType verificationType, User user, String sendTo);



}
