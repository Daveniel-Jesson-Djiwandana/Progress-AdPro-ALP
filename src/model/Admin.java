package model;

public class Admin extends Account {
    private String name;
    private String email;
    private String phoneNumber;
    private String adminID;
    private String rank;
    private FireStation assignedStation;

    public Admin(String name, String email, String username, String password, String phoneNumber, String adminID, String rank) {
        super(username, password);
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.adminID = adminID;
        this.rank = rank;
    }

    @Override
    public void view_incident_status() {}

    @Override
    public void view_report_history() {}

    public void fire_incident_randomizer() {}

    public void configure_dispatch_priority_rules() {}

    public void dispatch_firetrucks() {}

    public void monitor_affected_civilians() {}

    public void manage_firetruck_resources() {}


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getRole() {
        return "admin";
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhone() {
        return phoneNumber;
    }

    public String getAdminID() {
        return adminID;
    }

    public String getBadgeNumber() {
        return adminID;
    }

    public String getRank() {
        return rank;
    }

    public FireStation getAssignedStation() {
        return assignedStation;
    }

    public void setAssignedStation(FireStation station) {
        this.assignedStation = station;
    }
}
