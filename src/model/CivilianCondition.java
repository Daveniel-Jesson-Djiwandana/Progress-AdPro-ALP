package model;

public enum CivilianCondition {
    SAFE("Aman"),
    INJURED("Luka-luka"),
    CRITICAL("Kritis"),
    EVACUATED("Dievakuasi");

    private final String label;

    CivilianCondition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
