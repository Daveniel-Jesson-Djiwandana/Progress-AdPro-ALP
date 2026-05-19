package model;

import java.util.ArrayList;

public class FireStation {
    private String name;
    private String address;
    private ArrayList<Admin> employees;    // required data structure
    private ArrayList<Firetruck> firetrucks; // required data structure

    public FireStation(String name, String address) {
        this.name = name;
        this.address = address;
        this.employees = new ArrayList<>();
        this.firetrucks = new ArrayList<>();
    }

    public void addEmployee(Admin admin)      { employees.add(admin); }
    public void addFiretruck(Firetruck truck) { firetrucks.add(truck); }

    public String getName()                      { return name; }
    public String getAddress()                   { return address; }
    public ArrayList<Admin> getEmployees()       { return employees; }
    public ArrayList<Firetruck> getFiretrucks()  { return firetrucks; }

    public int getAvailableTruckCount() {
        int count = 0;
        for (Firetruck t : firetrucks) {
            if (t.getStatus() == TruckStatus.AVAILABLE) count++;
        }
        return count;
    }

    public ArrayList<Firetruck> getAvailableTrucks() {
        ArrayList<Firetruck> list = new ArrayList<>();
        for (Firetruck t : firetrucks) {
            if (t.getStatus() == TruckStatus.AVAILABLE) list.add(t);
        }
        return list;
    }
}
