package com.eglise.secretariat.services.api;

import com.eglise.secretariat.config.AppConfig;
import com.eglise.secretariat.config.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static ApiClient instance;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(AppConfig.CONNECT_TIMEOUT_SECONDS))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // --- Helper for URL building ---
    private String buildUrl(String endpoint, Map<String, Object> queryParams) {
        StringBuilder url = new StringBuilder(AppConfig.getApiBaseUrl()).append(endpoint);
        if (queryParams != null && !queryParams.isEmpty()) {
            url.append("?");
            boolean first = true;
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                if (entry.getValue() != null) {
                    if (!first) {
                        url.append("&");
                    }
                    url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                       .append("=")
                       .append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
                    first = false;
                }
            }
        }
        return url.toString();
    }

    // --- Add common headers (Auth token, content type) ---
    private HttpRequest.Builder createRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(AppConfig.REQUEST_TIMEOUT_SECONDS))
                .header("Accept", "application/json");

        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }

    // --- Generic GET (Async) ---
    public <T> CompletableFuture<T> getAsync(String endpoint, Map<String, Object> queryParams, Class<T> responseType) {
        String url = buildUrl(endpoint, queryParams);
        HttpRequest request = createRequestBuilder(url).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::validateResponse)
                .thenApply(body -> parseJson(body, responseType));
    }

    public <T> CompletableFuture<T> getAsync(String endpoint, Map<String, Object> queryParams, TypeReference<T> responseType) {
        String url = buildUrl(endpoint, queryParams);
        HttpRequest request = createRequestBuilder(url).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::validateResponse)
                .thenApply(body -> parseJson(body, responseType));
    }

    // --- Generic POST (Async) ---
    public <T> CompletableFuture<T> postAsync(String endpoint, Object requestBody, Class<T> responseType) {
        String url = buildUrl(endpoint, null);
        try {
            String json = requestBody != null ? objectMapper.writeValueAsString(requestBody) : "";
            HttpRequest request = createRequestBuilder(url)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::validateResponse)
                    .thenApply(body -> parseJson(body, responseType));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new ApiException("Erreur de sérialisation JSON: " + e.getMessage(), e));
        }
    }

    // --- Generic PUT (Async) ---
    public <T> CompletableFuture<T> putAsync(String endpoint, Object requestBody, Class<T> responseType) {
        String url = buildUrl(endpoint, null);
        try {
            String json = requestBody != null ? objectMapper.writeValueAsString(requestBody) : "";
            HttpRequest request = createRequestBuilder(url)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::validateResponse)
                    .thenApply(body -> parseJson(body, responseType));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new ApiException("Erreur de sérialisation JSON: " + e.getMessage(), e));
        }
    }

    // --- Generic PATCH (Async) ---
    public <T> CompletableFuture<T> patchAsync(String endpoint, Map<String, Object> queryParams, Class<T> responseType) {
        String url = buildUrl(endpoint, queryParams);
        HttpRequest request = createRequestBuilder(url)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::validateResponse)
                .thenApply(body -> parseJson(body, responseType));
    }

    // --- Generic DELETE (Async) ---
    public CompletableFuture<Void> deleteAsync(String endpoint) {
        String url = buildUrl(endpoint, null);
        HttpRequest request = createRequestBuilder(url).DELETE().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::validateResponse)
                .thenApply(body -> null);
    }

    // --- Download PDF / Raw Bytes (Async) ---
    public CompletableFuture<byte[]> getBytesAsync(String endpoint, Map<String, Object> queryParams) {
        String url = buildUrl(endpoint, queryParams);
        HttpRequest request = createRequestBuilder(url)
                .header("Accept", "application/pdf, application/octet-stream, */*")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return response.body();
                    }
                    String bodyStr = new String(response.body(), StandardCharsets.UTF_8);
                    String msg = extractErrorMessage(response.statusCode(), bodyStr);
                    throw new RuntimeException(new ApiException(response.statusCode(), msg, bodyStr));
                });
    }

    // --- Post for PDF Generation (Async) ---
    public CompletableFuture<byte[]> postForBytesAsync(String endpoint, Object requestBody) {
        String url = buildUrl(endpoint, null);
        try {
            String json = requestBody != null ? objectMapper.writeValueAsString(requestBody) : "";
            HttpRequest request = createRequestBuilder(url)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/pdf, application/octet-stream, */*")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            return response.body();
                        }
                        String bodyStr = new String(response.body(), StandardCharsets.UTF_8);
                        String msg = extractErrorMessage(response.statusCode(), bodyStr);
                        throw new RuntimeException(new ApiException(response.statusCode(), msg, bodyStr));
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new ApiException("Erreur lors de la requête PDF: " + e.getMessage(), e));
        }
    }

    // --- Multipart Upload for OCR / Image Scan (Async) ---
    public <T> CompletableFuture<T> postMultipartAsync(String endpoint, File file, Class<T> responseType) {
        String url = buildUrl(endpoint, null);
        String boundary = "---" + UUID.randomUUID().toString();

        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            StringBuilder headerBuilder = new StringBuilder();
            headerBuilder.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n")
                    .append("Content-Type: ").append(mimeType).append("\r\n\r\n");

            byte[] headerBytes = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);
            byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

            byte[] payload = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
            System.arraycopy(headerBytes, 0, payload, 0, headerBytes.length);
            System.arraycopy(fileBytes, 0, payload, headerBytes.length, fileBytes.length);
            System.arraycopy(footerBytes, 0, payload, headerBytes.length + fileBytes.length, footerBytes.length);

            HttpRequest request = createRequestBuilder(url)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::validateResponse)
                    .thenApply(body -> parseJson(body, responseType));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(new ApiException("Impossible de lire le fichier: " + e.getMessage(), e));
        }
    }

    // --- Internal Validation & JSON Parsing ---
    private String validateResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        String body = response.body();

        if (status >= 200 && status < 300) {
            return body;
        }

        String userFriendlyMsg = extractErrorMessage(status, body);
        throw new RuntimeException(new ApiException(status, userFriendlyMsg, body));
    }

    private String extractErrorMessage(int status, String body) {
        if (body != null && !body.trim().isEmpty()) {
            try {
                Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
                if (map.containsKey("errors") && map.get("errors") != null) {
                    return String.valueOf(map.get("errors"));
                }
                if (map.containsKey("message") && map.get("message") != null && !String.valueOf(map.get("message")).isBlank()) {
                    return String.valueOf(map.get("message"));
                }
            } catch (Exception ignored) {}
        }
        if (status == 401) return "Identifiants invalides ou session expirée.";
        if (status == 403) return "Accès refusé. Droits insuffisants.";
        if (status == 404) return "Ressource introuvable sur le serveur.";
        if (status >= 500) return "Erreur du serveur backend (500). Vérifiez les logs du backend.";
        return "Erreur HTTP " + status;
    }

    private <T> T parseJson(String body, Class<T> responseType) {
        if (responseType == Void.class || responseType == void.class || body == null || body.trim().isEmpty()) {
            return null;
        }
        if (responseType == String.class) {
            @SuppressWarnings("unchecked")
            T casted = (T) body;
            return casted;
        }
        try {
            return objectMapper.readValue(body, responseType);
        } catch (Exception e) {
            throw new RuntimeException(new ApiException("Erreur de parsing JSON: " + e.getMessage(), e));
        }
    }

    private <T> T parseJson(String body, TypeReference<T> responseType) {
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, responseType);
        } catch (Exception e) {
            throw new RuntimeException(new ApiException("Erreur de parsing JSON: " + e.getMessage(), e));
        }
    }
}
