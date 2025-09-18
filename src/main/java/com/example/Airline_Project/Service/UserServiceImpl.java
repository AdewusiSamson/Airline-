package com.example.Airline_Project.Service;

import com.example.Airline_Project.Domain.VerificationType;
import com.example.Airline_Project.Repository.UserRepository;
import com.example.Airline_Project.configuration.JwtProvider;
import com.example.Airline_Project.model.User;
import com.example.Airline_Project.model.twoFactorAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findUserProfileByJwt(String jwt) throws Exception {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        if (jwt == null || jwt.trim().isEmpty()) {
            throw new Exception("JWT token is null or empty");
        }

        String email = JwtProvider.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found");
        }
        return user;
    }
    @Override
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found");
        }
        return user;
    }

    @Override
    public User findUserById(Long userid) throws Exception {
        Optional<User> user = userRepository.findById(userid);
        if (user.isEmpty()) {
            throw new Exception("user not found");

        }
        return user.get();
    }

    @Override
    public User enableTwoFactorAuthentication(VerificationType verificationType, User user, String sendTo) {
        twoFactorAuth twoFactorAuth = new twoFactorAuth();
        twoFactorAuth.setEnabled(true);
        twoFactorAuth.setSendTo(verificationType);
        user.setTwoFactorAuth(twoFactorAuth);
        return userRepository.save(user);
    }

    @Override
    public User updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode( newPassword));
        return userRepository.save(user);
    }
}
