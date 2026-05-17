package database;

import model.*;
import java.util.HashMap;

public class Database {

    private static final HashMap<String, Account> users = new HashMap<>();
    private static FireStation fireStation = null;
    private static Account currentUser = null;

    // This block triggers as soon as the app starts
    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        // We initialize the station here so it's never null
        fireStation = new FireStation("Dinas Pemadam Kebakaran Kota", "Jl. Sudirman No. 10, Jakarta");

        // Only add your specific dummy data if the text file didn't load anything first
        if (users.isEmpty()) {
            initDummyData();
        }
    }

    private static void initDummyData() {
        // Your exact dummy data setup
        Admin a1 = new Admin("Kepala Regu A", "admin@pemadam.id", "admin", "admin123", "08111234567", "PK-001", "Kepala Regu");
        Admin a2 = new Admin("Sersan Ahmad", "ahmad@pemadam.id", "ahmad", "ahmad123", "08222345678", "PK-002", "Sersan");
        Admin a3 = new Admin("Pemadam Budi", "budi@pemadam.id", "budi", "budi123", "08333456789", "PK-003", "Anggota");
        
        for (Admin a : new Admin[]{a1, a2, a3}) { 
            users.put(a.getUsername(), a); 
            fireStation.addEmployee(a); 
        }

        User u1 = new User("Warga Biasa", "warga@email.com", "user", "user123", "08444567890", "user");
        users.put(u1.getUsername(), u1);
    }

    // --- Standard Methods ---

    public static HashMap<String, Account> getUsers() {
        return users;
    }

    public static Account findUser(String username) {
        return users.get(username);
    }

    public static void addUser(Account account) {
        if (account != null) {
            users.put(account.getUsername(), account);
        }
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    public static Account getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Account a) {
        currentUser = a;
    }

    public static void logout() {
        currentUser = null;
    }

    public static FireStation getFireStation() {
        return fireStation;
    }
}