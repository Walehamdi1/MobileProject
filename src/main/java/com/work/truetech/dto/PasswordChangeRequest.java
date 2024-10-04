package com.work.truetech.dto;

import lombok.Getter;

@Getter
public class PasswordChangeRequest {
    // Getters and setters
    private String oldPassword;
    private String newPassword;

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

