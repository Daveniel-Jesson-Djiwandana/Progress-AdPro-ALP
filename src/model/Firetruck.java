package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Firetruck {
    private String id;
    private String plateNumber;
    private int waterCapacity;
    private int currentWater;
    private int fuelLevel; // 0-100
    private TruckStatus status;
    private FiretruckType type;
    private HashMap<Resource, Integer> resources; // required data structure
    private ArrayList<Admin> crew;                // required data structure

    public Firetruck(String id, String plateNumber, int waterCapacity, int currentWater, int fuelLevel, FiretruckType type) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.waterCapacity = waterCapacity;
        this.currentWater = currentWater;
        this.fuelLevel = fuelLevel;
        this.status = TruckStatus.AVAILABLE;
        this.type = type;
        this.resources = new HashMap<>();
        this.crew = new ArrayList<>();

        // Initialise default resources
        resources.put(Resource.WATER,         currentWater);
        resources.put(Resource.FOAM,          200);
        resources.put(Resource.HOSE,          4);
        resources.put(Resource.LADDER,        2);
        resources.put(Resource.OXYGEN_TANK,   6);
        resources.put(Resource.FIRST_AID_KIT, 3);
    }

    public String getId()           { return id; }
    public String getPlateNumber()  { return plateNumber; }
    public int getWaterCapacity()   { return waterCapacity; }
    public int getCurrentWater()    { return currentWater; }
    public int getFuelLevel()       { return fuelLevel; }
    public TruckStatus getStatus()  { return status; }
    public FiretruckType getType()  { return type; }
    public void setType(FiretruckType type) { this.type = type; }
    public HashMap<Resource, Integer> getResources() { return resources; }
    public ArrayList<Admin> getCrew() { return crew; }

    public void setCurrentWater(int w) {
        this.currentWater = w;
        resources.put(Resource.WATER, w);
    }
    public void setFuelLevel(int f)       { this.fuelLevel = f; }
    public void setStatus(TruckStatus s)  { this.status = s; }
    public void setResource(Resource r, int amount) { resources.put(r, amount); }
    public void addCrewMember(Admin a)    { crew.add(a); }

    public String getDisplayName() { return id + " (" + plateNumber + ")"; }
}
