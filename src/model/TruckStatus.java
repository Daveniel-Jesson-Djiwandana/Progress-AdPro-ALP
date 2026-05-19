package model;

public enum TruckStatus {
    AVAILABLE("Tersedia"),
    DEPLOYED("Sedang Bertugas"),
    MAINTENANCE("Dalam Perawatan");

    private final String label;

    TruckStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
