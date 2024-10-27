package com.work.truetech.config;

public class JwtTokenExpiredException extends RuntimeException {

    public JwtTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
