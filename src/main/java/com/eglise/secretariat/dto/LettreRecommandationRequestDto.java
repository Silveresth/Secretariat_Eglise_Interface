package com.eglise.secretariat.dto;

import java.time.LocalDate;

public class LettreRecommandationRequestDto {
    private Long fideleId;
    private String motif;
    private String egliseDestination;
    private String pasteurSignataire;
    private LocalDate dateDepart;

    public LettreRecommandationRequestDto() {}

    public LettreRecommandationRequestDto(Long fideleId, String motif, String egliseDestination, String pasteurSignataire) {
        this.fideleId = fideleId;
        this.motif = motif;
        this.egliseDestination = egliseDestination;
        this.pasteurSignataire = pasteurSignataire;
        this.dateDepart = LocalDate.now();
    }

    public LettreRecommandationRequestDto(Long fideleId, String motif, String egliseDestination, String pasteurSignataire, LocalDate dateDepart) {
        this.fideleId = fideleId;
        this.motif = motif;
        this.egliseDestination = egliseDestination;
        this.pasteurSignataire = pasteurSignataire;
        this.dateDepart = dateDepart;
    }

    public Long getFideleId() {
        return fideleId;
    }

    public void setFideleId(Long fideleId) {
        this.fideleId = fideleId;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getEgliseDestination() {
        return egliseDestination;
    }

    public void setEgliseDestination(String egliseDestination) {
        this.egliseDestination = egliseDestination;
    }

    public String getPasteurSignataire() {
        return pasteurSignataire;
    }

    public void setPasteurSignataire(String pasteurSignataire) {
        this.pasteurSignataire = pasteurSignataire;
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDate dateDepart) {
        this.dateDepart = dateDepart;
    }
}
