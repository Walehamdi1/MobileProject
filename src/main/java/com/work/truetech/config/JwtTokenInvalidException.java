package com.work.truetech.config;

public class JwtTokenInvalidException extends RuntimeException {

    // Correct constructor name to match the class name
    public JwtTokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
