package model;

public enum IncidentSeverity {
    UNDETERMINED("Belum Ditentukan"),
    RED("Red (Kecil)"),
    DOUBLE_RED("Double Red (Sedang)"),
    TRIPLE_RED("Triple Red (Besar)");

    private final String label;

    IncidentSeverity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
