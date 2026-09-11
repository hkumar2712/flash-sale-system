package com.hims.flashsale.userservice.exception;

/**
 * A dedicated, specific exception rather than throwing a generic RuntimeException
 * with a message string. This matters because:
 *   - Calling code (or a global exception handler) can catch THIS specific type
 *     and react appropriately (e.g. map it to HTTP 409 Conflict), instead of
 *     accidentally catching and mishandling unrelated errors.
 *   - It documents intent - the name itself explains what went wrong, both to
 *     future-you reading the code and to anyone reviewing it.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists");
    }
}