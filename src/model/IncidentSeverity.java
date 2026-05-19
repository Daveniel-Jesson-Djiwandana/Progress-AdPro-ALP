package model;

public enum IncidentSeverity {
    LOW("Rendah"),
    MEDIUM("Sedang"),
    HIGH("Tinggi"),
    CRITICAL("Kritis");

    private final String label;

    IncidentSeverity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
