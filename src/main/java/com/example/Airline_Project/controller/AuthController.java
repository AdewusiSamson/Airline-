package com.example.Airline_Project.controller;

import com.example.Airline_Project.OtpUtils;
import com.example.Airline_Project.Repository.UserRepository;
import com.example.Airline_Project.Response.AuthResponse;
import com.example.Airline_Project.Service.CustomUserDetailsSerivce;
import com.example.Airline_Project.Service.EmailService;
import com.example.Airline_Project.configuratiion.JwtProvider;
import com.example.Airline_Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomUserDetailsSerivce customUserDetailsSerivce;


    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) throws Exception {

        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if (isEmailExist != null) {
            throw new Exception("email already exist");
        }
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFirstName(user.getFirstName()
        );
        newUser.setLastName(user.getLastName());
        newUser.setPassword(user.getPassword());
        User savedUser = userRepository.save(newUser);


        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword()

        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = JwtProvider.generateTokens(auth);
        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("success");

        return new ResponseEntity<>(res, HttpStatus.CREATED);


    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> Login(@RequestBody User user) throws Exception {

        String username = user.getEmail();
        String password = user.getPassword();


        Authentication auth = authenticate(username, password);


        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = JwtProvider.generateTokens(auth);
        User authuser = userRepository.findByEmail(username);

        String otp = OtpUtils.generateOTP();

            emailService.sendOtpEmail(username, otp);


        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("login success");

        return new ResponseEntity<>(res, HttpStatus.CREATED);


    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customUserDetailsSerivce.loadUserByUsername(username);
        if (userDetails == null) {
            throw new BadCredentialsException("invalid username");
        }
        if (!password.equals(userDetails.getPassword())) {
            throw new BadCredentialsException("invalid password");


        }
        return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
    }
}