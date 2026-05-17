package model;

public class User extends Account {
    private String name;
    private String email;
    private String phoneNumber;
    public User(String name, String email, String username, String password, String phoneNumber, String role) {
        super(username, password);
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void view_incident_status() {}

    @Override
    public void view_report_history() {}

    public void report_fire_incident() {}

    // Getter
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
        return "user";
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhone() {
        return phoneNumber;
    }
}
