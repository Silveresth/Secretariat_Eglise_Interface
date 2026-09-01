package com.eglise.secretariat.services;

import com.eglise.secretariat.config.SessionManager;
import com.eglise.secretariat.dto.AuthResponseDto;
import com.eglise.secretariat.dto.LoginRequestDto;
import com.eglise.secretariat.dto.UpdateProfileDto;
import com.eglise.secretariat.services.api.ApiClient;

import java.util.concurrent.CompletableFuture;

public class AuthService {

    private final ApiClient apiClient = ApiClient.getInstance();

    public CompletableFuture<AuthResponseDto> login(String usernameOrEmail, String password) {
        LoginRequestDto request = new LoginRequestDto(usernameOrEmail, password);
        return apiClient.postAsync("/api/auth/login", request, AuthResponseDto.class)
                .thenApply(response -> {
                    if (response != null && response.getToken() != null) {
                        SessionManager.getInstance().setSession(
                                response.getToken(),
                                usernameOrEmail,
                                response.getRole(),
                                usernameOrEmail.contains("@") ? usernameOrEmail : usernameOrEmail + "@eglise.org"
                        );
                    }
                    return response;
                });
    }

    public CompletableFuture<String> updateProfile(String currentPassword, String newUsername, String newPassword) {
        UpdateProfileDto request = new UpdateProfileDto(currentPassword, newUsername, newPassword);
        return apiClient.putAsync("/api/auth/profile", request, String.class)
                .thenApply(res -> {
                    if (newUsername != null && !newUsername.trim().isEmpty()) {
                        SessionManager.getInstance().setUsername(newUsername);
                    }
                    return res;
                });
    }

    public void logout() {
        SessionManager.getInstance().clearSession();
    }
}
