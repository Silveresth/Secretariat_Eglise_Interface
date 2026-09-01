package com.eglise.secretariat.dto;

import java.time.LocalDate;

public class OcrResultDto {
    private String pasteurSignataire;
    private String egliseOrigine;
    private LocalDate dateLettre;
    private String rawText;

    public OcrResultDto() {}

    public OcrResultDto(String pasteurSignataire, String egliseOrigine, LocalDate dateLettre, String rawText) {
        this.pasteurSignataire = pasteurSignataire;
        this.egliseOrigine = egliseOrigine;
        this.dateLettre = dateLettre;
        this.rawText = rawText;
    }

    public String getPasteurSignataire() {
        return pasteurSignataire;
    }

    public void setPasteurSignataire(String pasteurSignataire) {
        this.pasteurSignataire = pasteurSignataire;
    }

    public String getEgliseOrigine() {
        return egliseOrigine;
    }

    public void setEgliseOrigine(String egliseOrigine) {
        this.egliseOrigine = egliseOrigine;
    }

    public LocalDate getDateLettre() {
        return dateLettre;
    }

    public void setDateLettre(LocalDate dateLettre) {
        this.dateLettre = dateLettre;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
