package com.hims.flashsale.userservice.config;

import com.hims.flashsale.userservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @Configuration marks this class as a source of Spring beans (similar in spirit to
 * @SpringBootApplication's @Configuration part, just a dedicated class for it).
 *
 * @EnableWebSecurity activates Spring Security's web support and gives us access to
 * HttpSecurity for building a custom filter chain - without it, we'd be stuck with
 * Spring Boot's auto-configured DEFAULT security setup (locks everything, random
 * generated login password), which we're deliberately replacing here.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * This is the actual security policy for the whole application - which URLs
     * are public, which require auth, and how our custom JWT filter plugs into
     * Spring Security's existing filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection defends against a browser-session/cookie-based attack
                // vector. We're building a STATELESS, token-based API (no cookies/sessions
                // involved in auth at all) - CSRF simply doesn't apply to this model, so
                // we disable it. (If we later added cookie-based auth, this decision
                // would need revisiting.)
                .csrf(csrf -> csrf.disable())

                // STATELESS is the crucial line for JWT-based auth: it tells Spring
                // Security "never create or use an HttpSession to remember who's logged
                // in." Every single request must carry its own proof of identity (the
                // JWT) - nothing is remembered server-side between requests. This is
                // WHY this approach scales horizontally: any instance of user-service
                // can validate any request without needing shared session storage.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public: no token required at all.
                        .requestMatchers("/hello", "/auth/register", "/auth/login").permitAll()
                        // Everything else: must be authenticated (valid JWT required).
                        .anyRequest().authenticated()
                )

                // Insert OUR filter into Spring Security's existing chain, specifically
                // BEFORE UsernamePasswordAuthenticationFilter (Spring Security's own
                // built-in filter for traditional form-login auth, which we're not using,
                // but its position in the chain is still a useful, well-known anchor point
                // to insert custom auth filters ahead of).
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}