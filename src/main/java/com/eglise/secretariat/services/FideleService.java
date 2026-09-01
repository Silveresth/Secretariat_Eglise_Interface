package com.eglise.secretariat.services;

import com.eglise.secretariat.dto.FideleDto;
import com.eglise.secretariat.dto.PageResponseDto;
import com.eglise.secretariat.services.api.ApiClient;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FideleService {

    private final ApiClient apiClient = ApiClient.getInstance();

    public CompletableFuture<PageResponseDto<FideleDto>> searchFideles(
            String query,
            String quartier,
            Boolean baptise,
            Boolean actif,
            int page,
            int size,
            String sort
    ) {
        Map<String, Object> params = new HashMap<>();
        if (query != null && !query.trim().isEmpty()) params.put("query", query.trim());
        if (quartier != null && !quartier.trim().isEmpty() && !quartier.equalsIgnoreCase("Tous")) params.put("quartier", quartier.trim());
        if (baptise != null) params.put("baptise", baptise);
        if (actif != null) params.put("actif", actif);
        params.put("page", page);
        params.put("size", size);
        if (sort != null && !sort.trim().isEmpty()) params.put("sort", sort);

        return apiClient.getAsync("/api/fideles", params, new TypeReference<PageResponseDto<FideleDto>>() {});
    }

    public CompletableFuture<FideleDto> getFideleById(Long id) {
        return apiClient.getAsync("/api/fideles/" + id, null, FideleDto.class);
    }

    public CompletableFuture<FideleDto> createFidele(FideleDto dto) {
        return apiClient.postAsync("/api/fideles", dto, FideleDto.class);
    }

    public CompletableFuture<FideleDto> updateFidele(Long id, FideleDto dto) {
        return apiClient.putAsync("/api/fideles/" + id, dto, FideleDto.class);
    }

    public CompletableFuture<Void> deleteFidele(Long id) {
        return apiClient.deleteAsync("/api/fideles/" + id);
    }
}
