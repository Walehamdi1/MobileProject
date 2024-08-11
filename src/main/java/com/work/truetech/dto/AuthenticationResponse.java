package com.work.truetech.dto;

public class AuthenticationResponse {
    private final String token;

    public AuthenticationResponse(String token) {
        this.token = token;
    }

    public String gettoken() {
        return token;
    }
}
