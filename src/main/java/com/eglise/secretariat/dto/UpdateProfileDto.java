package com.eglise.secretariat.dto;

public class UpdateProfileDto {
    private String currentPassword;
    private String newUsername;
    private String newPassword;

    public UpdateProfileDto() {}

    public UpdateProfileDto(String currentPassword, String newUsername, String newPassword) {
        this.currentPassword = currentPassword;
        this.newUsername = newUsername;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
