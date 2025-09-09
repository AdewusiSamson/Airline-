package com.example.Airline_Project.controller;

import com.example.Airline_Project.OtpUtils;
import com.example.Airline_Project.Repository.UserRepository;
import com.example.Airline_Project.Response.AuthResponse;
import com.example.Airline_Project.Service.*;
import com.example.Airline_Project.configuratiion.JwtProvider;
import com.example.Airline_Project.model.SocialAuth;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;

    @Autowired
    private TwoFactorotpService twoFactorotpService;
    @Autowired
    private CustomUserDetailsSerivce customUserDetailsSerivce;
    @Autowired
    private SocialAuthService socialAuthService;
    @Autowired
    private JwtProvider jwtProvider;


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

    @PostMapping("/{provider}")
    public ResponseEntity<AuthResponse> socialLogin(
            @PathVariable String provider,
            @RequestBody Map<String, String> socialAuthRequest) {

        try {
            String providerId = socialAuthRequest.get("providerId");
            String email = socialAuthRequest.get("email");
            String firstName = socialAuthRequest.get("firstName");
            String lastName = socialAuthRequest.get("lastName");

            // Check if social auth exists
            SocialAuth socialAuth = socialAuthService.findSocialAuth(provider.toUpperCase(), providerId);
            User user;

            if (socialAuth != null) {
                // Existing user
                user = socialAuth.getUser();
            } else {
                // New user - check if email exists
                try {
                    user = userService.findUserByEmail(email);
                } catch (Exception e) {
                    // Create new user
                    user = new User();
                    user.setEmail(email);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setPassword(UUID.randomUUID().toString()); // Random password for social users
                    // You'll need to save the user through your user service
                }

                // Create social auth connection
                socialAuthService.createSocialAuth(user, provider.toUpperCase(), providerId, email);
            }

            // Generate JWT
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            String jwt = jwtProvider.generateTokens(auth);

            AuthResponse res = new AuthResponse();
            res.setJwt(jwt);
            res.setStatus(true);
            res.setMessage(provider + " login success");

            return new ResponseEntity<>(res, HttpStatus.OK);

        } catch (Exception e) {
            AuthResponse res = new AuthResponse();
            res.setStatus(false);
            res.setMessage("Social login failed: " + e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{provider}/unlink")
    public ResponseEntity<String> unlinkSocialAccount(
            @PathVariable String provider,
            @RequestHeader("Authorization") String jwt) {
        try {
            String token = jwt.substring(7); // Remove "Bearer " prefix
            String email = JwtProvider.getEmailFromToken(token);
            User user = userService.findUserByEmail(email);

            socialAuthService.unlinkSocialAuth(user, provider.toUpperCase());
            return ResponseEntity.ok("Social account unlinked successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error unlinking social account");
        }
    }
}