package com.eglise.secretariat.services;

import com.eglise.secretariat.dto.DashboardStatsDto;
import com.eglise.secretariat.services.api.ApiClient;

import java.util.concurrent.CompletableFuture;

public class DashboardService {

    private final ApiClient apiClient = ApiClient.getInstance();

    public CompletableFuture<DashboardStatsDto> getStats() {
        return apiClient.getAsync("/api/dashboard/stats", null, DashboardStatsDto.class);
    }
}
