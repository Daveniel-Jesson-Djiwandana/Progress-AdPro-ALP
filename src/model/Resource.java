package model;

public enum Resource {
    WATER("Air (L)"),
    FOAM("Busa Pemadam (L)"),
    HOSE("Selang"),
    LADDER("Tangga"),
    OXYGEN_TANK("Tabung Oksigen"),
    FIRST_AID_KIT("Kotak P3K");

    private final String displayName;

    Resource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
