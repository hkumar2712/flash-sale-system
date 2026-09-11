package com.hims.flashsale.userservice.controller;

import com.hims.flashsale.userservice.dto.RegisterRequest;
import com.hims.flashsale.userservice.dto.RegisterResponse;
import com.hims.flashsale.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notice how THIN this controller is - it does exactly three things:
 * validate input shape (via @Valid), delegate to the service layer, and translate
 * the result into an HTTP response. All actual business logic lives in UserService.
 * This separation is what makes the service layer independently testable without
 * spinning up any HTTP machinery at all.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @Valid triggers the jakarta.validation checks we put on RegisterRequest
     * (@NotBlank, @Email, @Size) BEFORE this method body even runs. If validation
     * fails, Spring throws MethodArgumentNotValidException automatically and returns
     * a 400 Bad Request - we don't have to write any of that checking ourselves.
     *
     * @RequestBody tells Spring to deserialize the incoming JSON request body
     * into a RegisterRequest object (via Jackson, auto-configured by
     * spring-boot-starter-web).
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request);

        // 201 Created is the semantically correct status for "a new resource was
        // successfully created" - not 200 OK, which implies something more generic.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}