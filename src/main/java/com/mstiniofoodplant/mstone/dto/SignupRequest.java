package com.mstiniofoodplant.mstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String email; // User email address
    private String password; // User password (will be hashed)
    private String username;
    private String firstName;
    private String lastName;
    private String middleName;
    private String role;
}
