package model;

import ui.UITheme;

public enum DamageLevel {
    RINGAN("Ringan", "< 25% area terdampak"),
    SEDANG("Sedang", "25-50% area terdampak"),
    BERAT("Berat", "50-75% area terdampak"),
    TOTAL("Total", "> 75% area terdampak");

    private final String label;
    private final String description;

    DamageLevel(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return label + " (" + description + ")";
    }
}
