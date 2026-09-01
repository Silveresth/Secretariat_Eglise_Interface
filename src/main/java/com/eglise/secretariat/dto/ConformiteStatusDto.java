package com.eglise.secretariat.dto;

public class ConformiteStatusDto {
    private Long fideleId;
    private Boolean carteMembreValide;
    private Boolean carnetDimeValide;
    private String message;

    public ConformiteStatusDto() {}

    public ConformiteStatusDto(Long fideleId, Boolean carteMembreValide, Boolean carnetDimeValide, String message) {
        this.fideleId = fideleId;
        this.carteMembreValide = carteMembreValide;
        this.carnetDimeValide = carnetDimeValide;
        this.message = message;
    }

    public Long getFideleId() {
        return fideleId;
    }

    public void setFideleId(Long fideleId) {
        this.fideleId = fideleId;
    }

    public Boolean getCarteMembreValide() {
        return carteMembreValide;
    }

    public void setCarteMembreValide(Boolean carteMembreValide) {
        this.carteMembreValide = carteMembreValide;
    }

    public Boolean getCarnetDimeValide() {
        return carnetDimeValide;
    }

    public void setCarnetDimeValide(Boolean carnetDimeValide) {
        this.carnetDimeValide = carnetDimeValide;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
