package com.hims.flashsale.userservice.exception;

/**
 * Deliberately vague message ("invalid email or password", not "no user found with
 * that email" vs "wrong password" as SEPARATE messages). This is intentional:
 * if we told an attacker specifically "that email doesn't exist" vs "wrong password",
 * we'd be leaking which emails ARE registered in our system (a real, common
 * vulnerability called a "user enumeration" attack). One generic message for both
 * cases closes that off entirely.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}