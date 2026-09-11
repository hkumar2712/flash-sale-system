package com.hims.flashsale.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Configuration marks this class as a source of Spring beans (similar in spirit to
 * @SpringBootApplication's @Configuration part, just a dedicated class for it).
 *
 * We define PasswordEncoder as an explicit @Bean (rather than "just calling
 * `new BCryptPasswordEncoder()` inline in UserService") so that:
 *   1. Any class can simply ask Spring for a PasswordEncoder via constructor injection,
 *      without knowing or caring that it's specifically BCrypt underneath.
 *   2. If we ever swap the hashing algorithm later, we change it in exactly ONE place.
 *
 * BCryptPasswordEncoder specifically: it's deliberately SLOW (by design, tunable via a
 * "strength"/work-factor parameter, default 10 rounds). Slowness is a FEATURE here -
 * it makes brute-forcing stolen password hashes computationally expensive, unlike fast
 * general-purpose hashes (MD5, SHA-256) which are actually the WRONG tool for passwords
 * precisely because they're fast.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}