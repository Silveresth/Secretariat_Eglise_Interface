package com.eglise.secretariat.services;

import com.eglise.secretariat.dto.LettreRecommandationRequestDto;
import com.eglise.secretariat.services.api.ApiClient;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DocumentService {

    private final ApiClient apiClient = ApiClient.getInstance();

    public CompletableFuture<byte[]> getFidelePdf(Long id) {
        return apiClient.getBytesAsync("/api/documents/fidele/" + id + "/pdf", null);
    }

    public CompletableFuture<byte[]> generateLettreRecommandationPdf(LettreRecommandationRequestDto request) {
        return apiClient.postForBytesAsync("/api/documents/lettre-recommandation/pdf", request);
    }

    public File savePdfToTemp(byte[] pdfBytes, String prefix) throws IOException {
        File tempFile = File.createTempFile(prefix + "_", ".pdf");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(pdfBytes);
        }
        return tempFile;
    }

    public void openPdf(File file) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            System.err.println("Impossible d'ouvrir le lecteur PDF: " + e.getMessage());
        }
    }
}
