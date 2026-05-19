package model;

public enum IncidentStatus {
    REPORTED("Dilaporkan"),
    DISPATCHED("Sedang Ditangani"),
    RESOLVED("Selesai");

    private final String label;

    IncidentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
