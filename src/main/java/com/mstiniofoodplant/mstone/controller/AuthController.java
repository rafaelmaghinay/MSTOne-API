package com.mstiniofoodplant.mstone.controller;

import com.mstiniofoodplant.mstone.dto.ApiResponse;
import com.mstiniofoodplant.mstone.dto.AuthResponse;
import com.mstiniofoodplant.mstone.dto.SignupRequest;
import com.mstiniofoodplant.mstone.dto.LoginRequest;
import com.mstiniofoodplant.mstone.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            logger.info("Signup attempt with email: {}", signupRequest.getEmail());

            if (signupRequest.getEmail() == null || signupRequest.getEmail().isEmpty() ||
                    signupRequest.getPassword() == null || signupRequest.getPassword().isEmpty()) {
                logger.warn("Signup attempt with empty email or password");
                return ResponseEntity
                        .badRequest()
                        .body(new ApiResponse(false, "Email and password are required"));
            }

            // Register new user with email and password
            AuthResponse authResponse = authService.registerUser(
                    signupRequest.getEmail(),
                    signupRequest.getPassword(),
                    signupRequest.getUsername(),
                    signupRequest.getFirstName(),
                    signupRequest.getLastName(),
                    signupRequest.getMiddleName(),
                    signupRequest.getRole());

            logger.info("User registered successfully: {}", authResponse.getEmail());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Signup successful", authResponse));

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in signup: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error during signup", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Signup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.loginUser(
                    loginRequest.getEmail(),
                    loginRequest.getPassword());

            return ResponseEntity.ok(new ApiResponse(true, "Login successful", authResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid email or password"));
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Login failed"));
        }
    }

}
