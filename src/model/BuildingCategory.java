package model;

/**
 * Kategori fungsi bangunan yang terbakar.
 * Digunakan dalam laporan kebakaran untuk analisis dan respons.
 */
public enum BuildingCategory {

    HUNIAN     ("Hunian",       "🏠"),
    KOMERSIAL  ("Komersial",    "🏪"),
    FASUM      ("Fasum / Fasos","🏫"),
    INDUSTRI   ("Industri",     "🏭"),
    INFRASTRUKTUR("Infrastruktur","🚉");

    private final String label;
    private final String icon;

    BuildingCategory(String label, String icon) {
        this.label = label;
        this.icon  = icon;
    }

    public String getLabel() { return label; }
    public String getIcon()  { return icon; }

    @Override
    public String toString() { return icon + " " + label; }

    /** Subtipe bangunan yang tersedia untuk kategori ini */
    public String[] getSubTypes() {
        switch (this) {
            case HUNIAN:
                return new String[]{
                    "Rumah tinggal", "Kos / kontrakan", "Apartemen / rusun"
                };
            case KOMERSIAL:
                return new String[]{
                    "Ruko", "Toko", "Pasar", "Mall", "Restoran / kafe", "Hotel"
                };
            case FASUM:
                return new String[]{
                    "Sekolah / kampus", "Rumah sakit / klinik",
                    "Tempat ibadah", "Gedung pemerintah"
                };
            case INDUSTRI:
                return new String[]{
                    "Pabrik", "Gudang", "Bengkel", "Gedung perkantoran"
                };
            case INFRASTRUKTUR:
                return new String[]{
                    "Stasiun / terminal", "Jembatan", "Pelabuhan / bandara"
                };
            default:
                return new String[]{"Lainnya"};
        }
    }
}
