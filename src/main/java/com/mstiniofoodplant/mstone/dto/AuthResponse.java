package com.mstiniofoodplant.mstone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String jwtToken;
    private UUID userId;
    private String email;
    private String firstName;
    private String role;
    private long expiresIn; // Token expiration time in milliseconds
    private String tokenType; // Usually "Bearer"

    public static AuthResponse of(String jwtToken, UUID userId, String email, String firstName, long expiresIn) {
        return of(jwtToken, userId, email, firstName, "Employee", expiresIn);
    }

    public static AuthResponse of(String jwtToken, UUID userId, String email, String firstName, String role, long expiresIn) {
        return AuthResponse.builder()
                .jwtToken(jwtToken)
                .userId(userId)
                .email(email)
                .firstName(firstName)
                .role(role)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .build();
    }
}