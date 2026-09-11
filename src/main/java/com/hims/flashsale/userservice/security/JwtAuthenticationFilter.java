package com.hims.flashsale.userservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * OncePerRequestFilter guarantees this filter's logic runs EXACTLY ONCE per request,
 * even in edge cases like internal servlet forwards/includes that could otherwise
 * trigger a filter chain twice - a subtle Servlet-API detail this base class handles
 * for us so we don't have to think about it.
 *
 * This filter sits IN FRONT of every controller. Its entire job: look at the incoming
 * request, and if it carries a valid JWT, tell Spring Security "this request is
 * authenticated, as this user, with this role" - by populating the SecurityContext.
 * If there's no token, or it's invalid/expired, we simply do nothing and let the
 * request continue unauthenticated; it's the SecurityFilterChain config (next file)
 * that decides whether an unauthenticated request is actually allowed to proceed
 * for the specific URL being requested.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No header, or doesn't start with "Bearer " -> nothing to do here,
        // just pass the request along unauthenticated.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Strip the "Bearer " prefix (7 characters) to get the raw token string.
        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.parseClaims(token);   // throws if invalid/expired/tampered

            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            // SimpleGrantedAuthority needs the "ROLE_" prefix by Spring Security convention -
            // this is what makes hasRole("ADMIN") (used later in 1.5) match a role of "ADMIN"
            // stored in our claim without that prefix.
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);

            // This is the actual "login" moment for this request, from Spring Security's
            // perspective - it now considers this request authenticated for its entire
            // lifecycle, purely based on what we just set here.
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception ex) {
            // Any parsing failure (expired, bad signature, malformed) - we deliberately
            // do NOT throw here. We just leave the request unauthenticated and let it
            // continue; the SecurityFilterChain's authorization rules will reject it
            // with a 401/403 if the target endpoint required authentication.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}