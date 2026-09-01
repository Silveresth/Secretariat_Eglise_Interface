package com.eglise.secretariat.dto;

import java.time.LocalDate;

public class EngagementDto {
    private Long id;
    private String type; // DEPARTEMENT, MINISTERE, GROUPE_VIE
    private String nom;
    private String role; // RESPONSABLE, ADJOINT, MEMBRE_ACTIF
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean actif = true;

    public EngagementDto() {}

    public EngagementDto(Long id, String type, String nom, String role, LocalDate dateDebut, LocalDate dateFin, boolean actif) {
        this.id = id;
        this.type = type;
        this.nom = nom;
        this.role = role;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.actif = actif;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
