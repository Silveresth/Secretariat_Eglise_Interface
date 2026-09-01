package com.eglise.secretariat.services;

import com.eglise.secretariat.dto.ConformiteStatusDto;
import com.eglise.secretariat.dto.FideleDto;
import com.eglise.secretariat.dto.OcrResultDto;
import com.eglise.secretariat.services.api.ApiClient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MouvementService {

    private final ApiClient apiClient = ApiClient.getInstance();

    public CompletableFuture<ConformiteStatusDto> updateCarteMembreStatus(Long fideleId, Boolean valide) {
        Map<String, Object> params = new HashMap<>();
        params.put("valide", valide);
        return apiClient.patchAsync("/api/mouvements/carte-membre/" + fideleId, params, ConformiteStatusDto.class);
    }

    public CompletableFuture<ConformiteStatusDto> updateCarnetDimeStatus(Long fideleId, Boolean valide) {
        Map<String, Object> params = new HashMap<>();
        params.put("valide", valide);
        return apiClient.patchAsync("/api/mouvements/carnet-dime/" + fideleId, params, ConformiteStatusDto.class);
    }

    public CompletableFuture<OcrResultDto> scanLetterOcr(File file) {
        return apiClient.postMultipartAsync("/api/mouvements/ocr-scan", file, OcrResultDto.class);
    }

    public CompletableFuture<FideleDto> saveFideleEntrant(FideleDto dto) {
        return apiClient.postAsync("/api/mouvements/fidele-entrant", dto, FideleDto.class);
    }
}
