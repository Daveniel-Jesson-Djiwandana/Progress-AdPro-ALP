package model;

import database.Database;

public abstract class Account {
    protected String username;
    protected String password;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public void register() {
        Database.addUser(this);
    }

    public abstract void view_incident_status();

    public abstract void view_report_history();

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getRole();

    public abstract boolean isAdmin();

    @Override
    public String toString() {
        return getName() + " (" + username + ")";
    }
}
