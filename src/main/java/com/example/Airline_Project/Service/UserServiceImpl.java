package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.UserRepository;
import com.example.Airline_Project.configuratiion.JwtProvider;
import com.example.Airline_Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User findUserProfileByJwt(String jwt) throws Exception {
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

}
