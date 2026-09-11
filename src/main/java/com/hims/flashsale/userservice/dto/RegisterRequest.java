package com.hims.flashsale.userservice.dto;

import com.hims.flashsale.userservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO = Data Transfer Object. This shape exists ONLY for the HTTP boundary -
 * it is deliberately NOT the same class as our User entity.
 *
 * Why not just accept a User directly in the controller? Two concrete reasons:
 * 1. User has a "passwordHash" field - if we bound HTTP input directly to User,
 *    a client could theoretically send a pre-computed "passwordHash" and skip
 *    real hashing entirely.
 * 2. User will later have fields like "id" or "createdAt" that are SERVER-GENERATED.
 *    A client should never be able to set its own id by including one in the JSON body.
 *
 * A dedicated RegisterRequest only exposes exactly the fields a client SHOULD provide.
 *
 * The jakarta.validation annotations below (@NotBlank, @Email, @Size) are declarative
 * validation rules - Spring will automatically reject malformed requests with a 400
 * error BEFORE our controller code even runs, as long as we annotate the controller
 * parameter with @Valid (we'll do that next).
 */
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    protected RegisterRequest() {
        // required for JSON deserialization (Jackson uses this + setters/reflection)
    }

    public RegisterRequest(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}