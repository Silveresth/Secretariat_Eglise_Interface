package com.eglise.secretariat.services.api;

public class ApiException extends Exception {
    private final int statusCode;
    private final String errorBody;

    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorBody = null;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorBody = null;
    }

    public ApiException(int statusCode, String message, String errorBody) {
        super(message);
        this.statusCode = statusCode;
        this.errorBody = errorBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public boolean isAuthError() {
        return statusCode == 401 || statusCode == 403;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }
}
