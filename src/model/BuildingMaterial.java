package model;

/**
 * Material dominan konstruksi bangunan yang terbakar.
 * Diisi oleh admin setelah menerima laporan.
 */
public enum BuildingMaterial {
    KAYU  ("Kayu",        "Mudah terbakar, risiko tinggi"),
    BETON ("Beton",       "Tahan api, risiko sedang"),
    BAJA  ("Baja / Besi", "Konduktor panas, risiko sedang"),
    CAMPURAN("Campuran",  "Material campuran");

    private final String label;
    private final String note;

    BuildingMaterial(String label, String note) {
        this.label = label;
        this.note  = note;
    }

    public String getLabel() { return label; }
    public String getNote()  { return note; }

    @Override
    public String toString() { return label; }
}
