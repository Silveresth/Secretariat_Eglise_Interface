package com.eglise.secretariat.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Sexe {
    MASCULIN("Masculin"),
    FEMININ("Féminin");

    private final String label;

    Sexe(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    @JsonCreator
    public static Sexe fromString(String value) {
        if (value == null) return null;
        String v = value.trim().toUpperCase();
        if (v.equals("M") || v.startsWith("MASC")) return MASCULIN;
        if (v.equals("F") || v.startsWith("FEM")) return FEMININ;
        try {
            return Sexe.valueOf(v);
        } catch (Exception e) {
            return MASCULIN;
        }
    }
}
