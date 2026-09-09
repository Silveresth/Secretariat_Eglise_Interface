package com.eglise.secretariat.services;

import com.eglise.secretariat.dto.FideleDto;
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

    public String buildDocumentFilename(FideleDto fidele, String documentTypeSuffix) {
        if (fidele == null) {
            return "Document_" + documentTypeSuffix;
        }
        String nom = fidele.getNom() != null ? fidele.getNom().trim().toUpperCase() : "";
        String prenoms = fidele.getPrenoms() != null ? fidele.getPrenoms().trim() : "";

        String namePart;
        if (!nom.isEmpty() && !prenoms.isEmpty()) {
            namePart = nom + "_" + prenoms;
        } else if (!nom.isEmpty()) {
            namePart = nom;
        } else if (!prenoms.isEmpty()) {
            namePart = prenoms;
        } else {
            namePart = "Fidele_" + (fidele.getId() != null ? fidele.getId() : "");
        }

        String cleanName = namePart.replaceAll("[\\\\/:*?\"<>|]", "").replaceAll("\\s+", "_");
        return cleanName + "_" + documentTypeSuffix;
    }

    public File getDownloadsDirectory() {
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        return downloadsDir;
    }

    public File savePdfToDownloads(byte[] pdfBytes, String filenamePrefix) throws IOException {
        File downloadsDir = getDownloadsDirectory();
        String cleanPrefix = (filenamePrefix != null && !filenamePrefix.isBlank()) ? filenamePrefix : "document";
        if (cleanPrefix.toLowerCase().endsWith(".pdf")) {
            cleanPrefix = cleanPrefix.substring(0, cleanPrefix.length() - 4);
        }

        File targetFile = new File(downloadsDir, cleanPrefix + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(pdfBytes);
        } catch (IOException e) {
            targetFile = new File(downloadsDir, cleanPrefix + "_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(pdfBytes);
            }
        }
        return targetFile;
    }

    public File savePdfToTemp(byte[] pdfBytes, String prefix) throws IOException {
        return savePdfToDownloads(pdfBytes, prefix);
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
