package model;

public enum FiretruckType {
    TYPE_1_WATER_MOTOR("Tipe 1 - Air/Motor"),
    TYPE_2_LADDER("Tipe 2 - Tangga"),
    TYPE_3_RESCUE("Tipe 3 - Rescue"),
    TYPE_4_HAZMAT("Tipe 4 - Hazmat"),
    TYPE_5_WATER_SUPPLY("Tipe 5 - Supply Air");

    private final String label;

    FiretruckType(String label) {
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
