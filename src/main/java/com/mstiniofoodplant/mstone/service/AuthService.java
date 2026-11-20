package com.mstiniofoodplant.mstone.service;

import com.mstiniofoodplant.mstone.controller.AuthController;
import com.mstiniofoodplant.mstone.dto.AuthResponse;
import com.mstiniofoodplant.mstone.model.Users;
import com.mstiniofoodplant.mstone.model.Roles;
import com.mstiniofoodplant.mstone.model.User_Roles;
import com.mstiniofoodplant.mstone.repository.RolesRepository;
import com.mstiniofoodplant.mstone.repository.User_RolesRepository;
import com.mstiniofoodplant.mstone.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private User_RolesRepository userRolesRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserEmployeeService userEmployeeService;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    @Transactional
    public AuthResponse registerUser(String email, String password, String username, String firstName, String lastName, String middleName, String role) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        logger.info("Attempting to register user: {}", email);

        // Check if user already exists
        Optional<Users> existingUser = userService.findByEmail(email);
        if (existingUser.isPresent()) {
            logger.warn("Registration failed - user already exists: {}", email);
            throw new IllegalArgumentException("Email already registered");
        }

        // Hash password
        String passwordHash = passwordEncoder.encode(password);

        // Create new user
        Users newUser = userService.createUser(email, username, firstName, lastName, middleName != null ? middleName : "", passwordHash);

        String requestedRole = role == null || role.trim().isEmpty() ? "Employee" : role.trim();
        Roles assignedRole = rolesRepository.findByRoleNameIgnoreCase(requestedRole)
                .or(() -> rolesRepository.findByRoleName(requestedRole))
                .orElseGet(() -> rolesRepository.findByRoleNameIgnoreCase("Employee")
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + requestedRole)));
        User_Roles userRole = new User_Roles();
        userRole.setUserId(newUser.getId());
        userRole.setRoleId(assignedRole.getId());
        userRolesRepository.save(userRole);

        // Automatically link user_employees if employee profile with matching email exists
        userEmployeeService.findOrAutoLinkEmployee(newUser.getId(), newUser.getEmail());

        logger.info("User registered successfully: {} ({}) with role {}", email, newUser.getId(), assignedRole.getRoleName());

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken(newUser.getId(), newUser.getEmail());
        long expirationTime = jwtTokenProvider.getExpirationTime();

        return AuthResponse.of(
                jwtToken,
                newUser.getId(),
                newUser.getEmail(),
                newUser.getFirstName(),
                assignedRole.getRoleName(),
                expirationTime);
    }

    @Transactional
    public AuthResponse loginUser(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        Users user = userService.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!user.isActive() || user.isLocked()
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userService.save(user);

        String roleName = userRolesRepository.findAllByUserId(user.getId()).stream()
                .findFirst()
                .flatMap(ur -> rolesRepository.findById(ur.getRoleId()))
                .map(Roles::getRoleName)
                .orElse("Employee");

        String jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return AuthResponse.of(
                jwtToken,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                roleName,
                jwtTokenProvider.getExpirationTime());
    }
}
