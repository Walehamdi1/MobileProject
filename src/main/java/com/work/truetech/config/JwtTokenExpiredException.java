package com.work.truetech.config;// File: JwtTokenExpiredException.java

public class JwtTokenExpiredException extends RuntimeException {

    public JwtTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
