package com.hims.flashsale.userservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * This endpoint exists purely to PROVE the security filter chain works end to end.
 * Notice we never manually check for a token here, never call JwtService directly -
 * by the time this method runs, JwtAuthenticationFilter has ALREADY validated the
 * token and populated Spring Security's context. All we do is read what's already
 * been established.
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        // Spring automatically injects the current request's Authentication object
        // here - this is only possible BECAUSE JwtAuthenticationFilter populated the
        // SecurityContext earlier in the chain. If it hadn't (no/invalid token), this
        // method would never even be reached - the request would be rejected with
        // 401/403 before getting this far, per our authorizeHttpRequests() rule.
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

        return Map.of(
                "email", email,
                "role", role
        );
    }
}