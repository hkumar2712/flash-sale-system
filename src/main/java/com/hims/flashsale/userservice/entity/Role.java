package com.hims.flashsale.userservice.entity;

/**
 * The set of roles a user can have. Used later for authorization
 * (e.g. @PreAuthorize("hasRole('ADMIN')")) once Spring Security is wired in.
 *
 * Keeping this as an enum (not a free-text String field) means the database
 * and the compiler both reject invalid roles - you can't accidentally save
 * a user with role "ADMNI" (typo) since it wouldn't compile.
 */
public enum Role {
    BUYER,
    SELLER,
    ADMIN
}
