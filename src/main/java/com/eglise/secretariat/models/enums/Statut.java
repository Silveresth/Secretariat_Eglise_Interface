package com.eglise.secretariat.models.enums;

public enum Statut {
    CELIBATAIRE("Célibataire"),
    MARIE("Marié(e)"),
    VEUF("Veuf/Veuve"),
    DIVORCE("Divorcé(e)");

    private final String label;

    Statut(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
