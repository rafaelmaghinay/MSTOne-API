package com.mstiniofoodplant.mstone.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class Users {

    /** Primary key for the user account. */
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    /** Unique username used to sign in. */
    @Column(length = 50, nullable = false)
    private String username;

    /** Email address associated with the account. */
    @Column(length = 150, nullable = false)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    /** Securely encrypted password hash. */
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;


    /** Whether the account is enabled for use. */
    @Column(name = "is_active", nullable = false)
    private boolean active;

    /** Whether the account is locked from signing in. */
    @Column(name = "is_locked", nullable = false)
    private boolean locked;

    /** Number of consecutive failed sign-in attempts. */
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    /** Date and time of the last successful sign-in. */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /** Date and time when the account was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Date and time when the account was last updated. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
