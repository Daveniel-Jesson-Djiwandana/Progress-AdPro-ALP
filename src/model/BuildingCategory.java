package model;

/**
 * Kategori fungsi bangunan yang terbakar.
 * Digunakan dalam laporan kebakaran untuk analisis dan respons.
 */
public enum BuildingCategory {

    BANGUNAN   ("Bangunan",     ""),
    INDUSTRI   ("Industri",     ""),
    LAHAN_KOSONG("Lahan Kosong", "");

    private final String label;
    private final String icon;

    BuildingCategory(String label, String icon) {
        this.label = label;
        this.icon  = icon;
    }

    public String getLabel() { return label; }
    public String getIcon()  { return icon; }

    @Override
    public String toString() { return label; }

    /** Subtipe bangunan/lahan yang tersedia untuk kategori ini */
    public String[] getSubTypes() {
        switch (this) {
            case BANGUNAN:
                return new String[]{
                    "Rumah tinggal", "Sekolah / Kampus", "Gedung Kantor", "Mall / Ruko", "Lainnya"
                };
            case INDUSTRI:
                return new String[]{
                    "Pabrik", "Gudang Bahan Kimia", "Gudang Plastik", "Gudang Minyak", "Bengkel", "Lainnya"
                };
            case LAHAN_KOSONG:
                return new String[]{
                    "Lahan Alang-alang", "Hutan Kota", "Kebun / Sawah", "Lahan Kosong Terbuka", "Lainnya"
                };
            default:
                return new String[]{"Lainnya"};
        }
    }
}
