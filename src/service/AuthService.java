package service;

import database.Database;
import model.*;
import java.io.*;
import java.util.Scanner;

public class AuthService {

    private static final String STORAGE_FILE = "users.txt";

    public static Account handleLogin(String username, String password) {
        Account account = Database.findUser(username);

        if (account != null && account.login(username, password)) {
            Database.setCurrentUser(account);
            return account;
        }
        return null;
    }

    
    public static String registerNewUser(String name, String email, String username,
            String password, String phone) {
        String error = validate(username, password);
        if (error != null)
            return error;

        User newUser = new User(name, email, username, password, phone, "user");
        newUser.register();
        saveToDisk(newUser);

        return null;
    }

    
    public static String registerNewAdmin(String name, String email, String username,
            String password, String phone,
            String badge, String rank) {
        String error = validate(username, password);
        if (error != null)
            return error;

        if (badge.isBlank() || rank.isBlank())
            return "Data petugas tidak lengkap.";

        Admin newAdmin = new Admin(name, email, username, password, phone, badge, rank);
        newAdmin.register();
        Database.getFireStation().addEmployee(newAdmin);
        saveToDisk(newAdmin);

        return null;
    }

    
    private static String validate(String username, String password) {
        if (username.isBlank() || password.isBlank())
            return "Input tidak boleh kosong.";
        if (Database.userExists(username))
            return "Username sudah terdaftar.";
        if (password.length() < 6)
            return "Password minimal 6 karakter.";
        return null;
    }

    
    private static void saveToDisk(Account account) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STORAGE_FILE, true))) {
            
            
            String phone = "N/A";
            if (account instanceof User) {
                phone = ((User) account).getPhoneNumber();
            } else if (account instanceof Admin) {
                phone = ((Admin) account).getPhoneNumber();
            }

            // role | username | password | name | email | phone
            String record = String.format("%s|%s|%s|%s|%s|%s",
                account.getRole(),
                account.getUsername(),
                account.getPassword(),
                account.getName(),
                account.getEmail(),
                phone
            );
            
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Gagal menyimpan ke file: " + e.getMessage());
        }
    }

    public static void loadUsersFromFile() {
        File file = new File(STORAGE_FILE);
        if (!file.exists()) return;

        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                
                String[] data = line.split("\\|");
                
                if (data.length >= 6) {
                    String role = data[0];
                    String username = data[1];
                    String password = data[2];
                    String name = data[3];
                    String email = data[4];
                    String phone = data[5];

                    if (role.equalsIgnoreCase("admin")) {
                        
                        Admin adminAccount = new Admin(name, email, username, password, phone, "N/A", "Staff");
                        
                        adminAccount.register(); 
                        Database.getFireStation().addEmployee(adminAccount);
                    } else {
                        
                        User userAccount = new User(name, email, username, password, phone, "user");
                        
                        userAccount.register(); 
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Data file not found, starting with empty database.");
        }
    }

    public static void processLogout() {
        Database.logout();
    }
}