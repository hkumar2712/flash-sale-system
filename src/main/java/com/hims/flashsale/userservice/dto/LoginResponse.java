package com.hims.flashsale.userservice.dto;

/**
 * "tokenType": "Bearer" is a convention from the OAuth2 spec - it tells the client
 * HOW to send this token back on future requests: as an Authorization header
 * formatted "Authorization: Bearer <token>". We're not implementing full OAuth2,
 * but reusing this convention keeps us compatible with how every HTTP client
 * library/tool (Postman, curl, frontend fetch libraries) already expects to handle
 * bearer tokens.
 */
public class LoginResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final long expiresInMs;

    public LoginResponse(String accessToken, long expiresInMs) {
        this.accessToken = accessToken;
        this.expiresInMs = expiresInMs;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }
}