package com.mstiniofoodplant.mstone.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias({ "access_type", "accesstype", "role_name", "rolename" })
    private String role;
}
