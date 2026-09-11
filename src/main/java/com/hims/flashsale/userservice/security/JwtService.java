package com.hims.flashsale.userservice.security;

import com.hims.flashsale.userservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Everything JWT-related lives in this ONE class - generation now, and validation
 * once we build the security filter in 1.4. Centralizing it means the signing key
 * and algorithm choice exist in exactly one place, not scattered across controllers.
 */
@Component
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    /**
     * @Value("${jwt.secret}") reads the "jwt.secret" property from application.yml
     * (which itself reads from the JWT_SECRET env var, or falls back to the default
     * we put there) and injects it as a constructor parameter - this is how Spring
     * wires externalized config into our own beans, not just its own auto-config.
     */
    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        // Keys.hmacShaKeyFor requires raw bytes of sufficient length for HS256 (>= 256 bits).
        // Our secret is stored as a Base64 STRING in config, so we decode it back to raw
        // bytes here before building the actual signing key object.
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Builds and signs a JWT for the given user.
     *
     * "Claims" are the actual data payload inside the token - visible to anyone who
     * Base64-decodes it (that part is NOT secret/encrypted), but the SIGNATURE
     * (computed using our secret key) proves the claims weren't tampered with after
     * we issued them. Anyone without the secret key cannot produce a signature that
     * will pass verification, even if they modify the payload.
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getEmail())                 // "sub" claim - WHO this token is about
                .claim("userId", user.getId().toString()) // custom claim - useful without a DB lookup
                .claim("role", user.getRole().name())      // custom claim - used for authorization later
                .issuedAt(now)                              // "iat" claim - when issued
                .expiration(expiry)                         // "exp" claim - when it stops being valid
                .signWith(signingKey)                       // computes the signature using our secret
                .compact();                                 // serializes to the final header.payload.signature string
    }

    /**
     * Parses and verifies a token, returning its claims if valid.
     * This throws an unchecked exception (e.g. ExpiredJwtException, SignatureException)
     * if the token is expired or has been tampered with - we're not using this yet in 1.3,
     * but it's the exact method the security filter in 1.4 will call on every incoming request.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}