package com.eglise.secretariat.config;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static SessionManager instance;

    private String token;
    private String username;
    private String role = "ADMIN";
    private String email;
    private boolean rememberMe = false;

    private final List<AuthStateListener> listeners = new ArrayList<>();

    public interface AuthStateListener {
        void onAuthStateChanged(boolean isAuthenticated);
    }

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setSession(String token, String username, String role, String email) {
        this.token = token;
        this.username = username != null ? username : "Secrétaire Général";
        this.role = role != null ? role : "ADMIN";
        this.email = email != null ? email : "admin@eglise.org";
        notifyListeners(true);
    }

    public void clearSession() {
        this.token = null;
        this.username = null;
        this.role = null;
        this.email = null;
        notifyListeners(false);
    }

    public boolean isAuthenticated() {
        return token != null && !token.trim().isEmpty();
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username != null ? username : "Secrétaire Général";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role != null ? role : "ADMIN";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email != null ? email : "admin@eglise.org";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public void addListener(AuthStateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(AuthStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(boolean isAuth) {
        for (AuthStateListener listener : listeners) {
            listener.onAuthStateChanged(isAuth);
        }
    }
}
