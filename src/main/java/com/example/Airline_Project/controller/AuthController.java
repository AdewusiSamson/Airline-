package com.example.Airline_Project.controller;

import com.example.Airline_Project.OtpUtils;
import com.example.Airline_Project.Repository.UserRepository;
import com.example.Airline_Project.Response.AuthResponse;
import com.example.Airline_Project.Service.CustomUserDetailsSerivce;
import com.example.Airline_Project.Service.EmailService;
import com.example.Airline_Project.Service.SocialAuthService;
import com.example.Airline_Project.Service.TwoFactorotpService;
import com.example.Airline_Project.configuratiion.JwtProvider;
import com.example.Airline_Project.model.TwoFactorOTP;
import com.example.Airline_Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
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
    private SocialAuthService socialAuthService;


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
        if (user.getTwoFactorAuth().isEnabled()) {
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

    @PostMapping("/two-factor/otp/{otp}")

    public ResponseEntity<AuthResponse> VerifySigninOtp(@PathVariable String otp, @RequestParam String id) throws Exception {
        TwoFactorOTP twoFactorOTP = twoFactorotpService.findById(id);
        if (twoFactorotpService.verifyTwoFactorOtp(twoFactorOTP, otp)) {
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor authentication Completed");
            res.setTwoFactorAuthEnabled(true);
            res.setJwt(twoFactorOTP.getJwt());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new Exception("invalid otp");
    }
//TODO social login
}