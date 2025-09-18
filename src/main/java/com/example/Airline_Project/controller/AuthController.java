package com.example.Airline_Project.controller;

import com.example.Airline_Project.OtpUtils;
import com.example.Airline_Project.Repository.UserRepository;
import com.example.Airline_Project.Response.AuthResponse;
import com.example.Airline_Project.Service.*;
import com.example.Airline_Project.configuration.JwtProvider;
import com.example.Airline_Project.model.SocialAuth;
import com.example.Airline_Project.model.TwoFactorOTP;
import com.example.Airline_Project.model.User;
import com.example.Airline_Project.request.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")

public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TwoFactorotpService twoFactorotpService;
    @Autowired
    private CustomUserDetailsSerivce customUserDetailsSerivce;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) throws Exception {

        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if (isEmailExist != null) {
            throw new Exception("email already exist");
        }
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
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
    public ResponseEntity<AuthResponse> Login(@RequestBody LoginRequest userRequest) throws Exception {
        String username = userRequest.getEmail();
        String password = userRequest.getPassword(); // Raw password

        Authentication auth = authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = JwtProvider.generateTokens(auth);
        User authuser = userRepository.findByEmail(username);

        // Check if the authenticated user has 2FA enabled (from database)
        if (authuser.getTwoFactorAuth() != null && authuser.getTwoFactorAuth().isEnabled()) {
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor auth is enabled");
            res.setTwoFactorAuthEnabled(true);
            String otp = OtpUtils.generateOTP();

            TwoFactorOTP oldTwoFactorOtp = twoFactorotpService.findByUser(authuser.getId());
            if (oldTwoFactorOtp != null) {
                twoFactorotpService.deleteTwoFactorOtp(oldTwoFactorOtp);
            }

            TwoFactorOTP newTwoFactorOTP = twoFactorotpService.createTwoFactorOtp(
                    authuser, otp, jwt);
            emailService.sendOtpEmail(username, otp);

            res.setSession(newTwoFactorOTP.getId());
            return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
        }

        // Regular login without 2FA
        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("login success");

        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customUserDetailsSerivce.loadUserByUsername(username);
        if (userDetails == null) {
            throw new BadCredentialsException("invalid username");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("invalid password");
        }
        return new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
    }

    @PostMapping("/two-factor/otp/{otp}")

    public ResponseEntity<AuthResponse> VerifySigninOtp(@PathVariable String otp, @RequestParam String id) throws Exception {
        TwoFactorOTP twoFactorOTP = twoFactorotpService.findById(id);
        if (twoFactorotpService.verifyTwoFactorOtp(twoFactorOTP, otp)) {
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor authentication Completed");
            res.setTwoFactorAuthEnabled(true);
            res.setJwt(twoFactorOTP.getJwt());
            res.setStatus(true);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new Exception("invalid otp");
    }

}
