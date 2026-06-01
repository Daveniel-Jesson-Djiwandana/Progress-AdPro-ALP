package model;

public class Civilian {
    private static int counter = 1;
    private int id;
    private String name;
    private CivilianCondition condition;
    private String locationInBuilding;

    public Civilian(String name, CivilianCondition condition, String locationInBuilding) {
        this.id = counter++;
        this.name = name;
        this.condition = condition;
        this.locationInBuilding = locationInBuilding;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CivilianCondition getCondition() {
        return condition;
    }

    public void setCondition(CivilianCondition c) {
        this.condition = c;
    }

    public String getLocationInBuilding() {
        return locationInBuilding;
    }
}
