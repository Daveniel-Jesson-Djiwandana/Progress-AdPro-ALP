package model;

import java.util.ArrayList;

public class FireStation {
    private String name;
    private String address;
    private ArrayList<Admin> employees;

    public FireStation(String name, String address) {
        this.name = name;
        this.address = address;
        this.employees = new ArrayList<>();
    
    }

    public void addEmployee(Admin admin)      { employees.add(admin); }
 
}
