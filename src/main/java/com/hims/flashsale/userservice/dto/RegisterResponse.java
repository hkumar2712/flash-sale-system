package com.hims.flashsale.userservice.dto;

import com.hims.flashsale.userservice.entity.Role;

import java.time.Instant;
import java.util.UUID;

/**
 * Just like RegisterRequest guards what comes IN, this guards what goes OUT.
 * Notice there is NO passwordHash field here - even though our User entity has one.
 * If our controller accidentally returned the User entity directly, Jackson (the JSON
 * library) would happily serialize passwordHash straight into the HTTP response body -
 * a real, common vulnerability class. Using a dedicated response DTO makes that
 * mistake structurally impossible: the field simply doesn't exist on this class.
 */
public class RegisterResponse {

    private final UUID id;
    private final String email;
    private final Role role;
    private final Instant createdAt;

    public RegisterResponse(UUID id, String email, Role role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}