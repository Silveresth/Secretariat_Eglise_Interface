package com.eglise.secretariat.models.enums;

public enum FrequenceDime {
    REGULIEREMENT("Régulièrement"),
    OCCASIONNELLEMENT("Occasionnellement"),
    RAREMENT("Rarement"),
    JAMAIS("Jamais");

    private final String label;

    FrequenceDime(String label) {
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
