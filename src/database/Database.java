package database;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;


public class Database {

    // HashMap users
    private static final HashMap<String, Account> users = new HashMap<>();

    // ArrayList incident and reports
    private static final ArrayList<Incident> allIncidents = new ArrayList<>();
    private static final ArrayList<ReportHistory> reportHistory = new ArrayList<>();

    // PriorityQueue
    private static final PriorityQueue<Incident> incidentQueue = new PriorityQueue<>();

    private static final ArrayList<FireStation> fireStations = new ArrayList<>();
    private static FireStationGraph roadNetwork = null;
    private static DispatchPriorityRules priorityRules = new DispatchPriorityRules();
    private static Account currentUser = null;

    static {
        initDemoData();
    }

    // Init

    private static void initDemoData() {
        String[][] stationData = {
                // Pusat
                { "Dinas Pemadam Kebakaran & Penyelamatan Surabaya", "Pusat Kota", "-7.24960", "112.73770", "Pusat",
                        "true" },
                { "Pos Damkar Pasar Turi", "Pasar Turi", "-7.24910", "112.73720", "Pusat", "false" },
                { "Pos Damkar Tegalsari", "Tegalsari", "-7.26000", "112.74200", "Pusat", "false" },
                { "Pos Damkar Genteng", "Genteng", "-7.25800", "112.74800", "Pusat", "false" },
                // Timur
                { "Pos Damkar Menur", "Menur", "-7.28520", "112.76010", "Timur", "true" },
                { "Pos Damkar Sukolilo", "Sukolilo", "-7.28940", "112.79130", "Timur", "false" },
                { "Pos Damkar Keputih", "Keputih", "-7.29210", "112.80540", "Timur", "false" },
                { "Pos Damkar Rungkut", "Rungkut", "-7.32350", "112.77680", "Timur", "false" },
                // Utara
                { "Pos Damkar Perak Barat", "Perak Barat", "-7.22680", "112.73690", "Utara", "true" },
                { "Pos Damkar Bulak", "Bulak", "-7.23890", "112.80010", "Utara", "false" },
                { "Pos Damkar Kenjeran", "Kenjeran", "-7.24180", "112.78120", "Utara", "false" },
                { "Pos Damkar Semampir", "Semampir", "-7.21000", "112.75500", "Utara", "false" },
                // Barat
                { "Pos Damkar Benowo", "Benowo", "-7.23190", "112.63980", "Barat", "true" },
                { "Pos Damkar Lakarsantri", "Lakarsantri", "-7.32390", "112.65780", "Barat", "false" },
                { "Pos Damkar Wiyung", "Wiyung", "-7.30820", "112.70150", "Barat", "false" },
                { "Pos Damkar Tandes", "Tandes", "-7.26500", "112.68000", "Barat", "false" },
                // Selatan
                { "Pos Damkar Karangpilang", "Karangpilang", "-7.33020", "112.68310", "Selatan", "true" },
                { "Pos Damkar Gunung Anyar", "Gunung Anyar", "-7.33610", "112.79520", "Selatan", "false" },
                { "Pos Damkar Demak Grudo", "Demak Grudo", "-7.27310", "112.73140", "Selatan", "false" },
                { "Pos Damkar Jambangan", "Jambangan", "-7.31500", "112.72000", "Selatan", "false" }
        };

        for (int i = 0; i < stationData.length; i++) {
            String name = stationData[i][0];
            String addr = stationData[i][1];
            double lat = Double.parseDouble(stationData[i][2]);
            double lon = Double.parseDouble(stationData[i][3]);
            String rayon = stationData[i][4];
            boolean isInduk = Boolean.parseBoolean(stationData[i][5]);
            FireStation station = new FireStation(name, addr, lat, lon, rayon, isInduk);
            fireStations.add(station);

            // Generate admin utk stationnya
            String locationShort = addr.toLowerCase().replace(" / ", "_").replace(" ", "_").replace("-", "_");
            String username = "admin_" + locationShort;
            String email = username + "@damkar.surabaya.id";
            String adminID = "PK-SBY-" + String.format("%03d", i + 1);

            Admin admin = new Admin(
                    "Kepala Pos " + addr,
                    email,
                    username,
                    "admin123",
                    "081" + String.format("%08d", i + 1),
                    adminID,
                    "Kepala Pos");
            admin.setAssignedStation(station);
            users.put(username, admin);
            station.addEmployee(admin);

            // Generate firetrucks
            for (int k = 1; k <= 5; k++) {
                String truckID = "PMK-" + addr.replace(" ", "") + "-" + k;
                String plate = "L " + (1000 + i * 10 + k) + " PMK";
                int cap = (k % 2 == 0) ? 3000 : 5000;
                int pressure = (k % 2 == 0) ? 80 : 100;
                FiretruckType type = FiretruckType.values()[k - 1];
                Firetruck truck = new Firetruck(truckID, plate, cap, cap, pressure, type);
                if (k == 1) {
                    truck.addCrewMember(admin);
                }
                station.addFiretruck(truck);
            }
        }

        // Super admin
        Admin legacyAdmin = new Admin("Super Admin", "admin@damkar.surabaya.id", "admin", "admin123", "08100000000",
                "PK-SBY-000", "Super");
        legacyAdmin.setAssignedStation(fireStations.get(0));
        users.put("admin", legacyAdmin);
        fireStations.get(0).addEmployee(legacyAdmin);

        Admin ahmad = new Admin("Sersan Ahmad", "ahmad@damkar.surabaya.id", "ahmad", "ahmad123", "08222345678",
                "PK-SBY-902", "Sersan");
        ahmad.setAssignedStation(fireStations.get(1));
        users.put("ahmad", ahmad);

        Admin budi = new Admin("Pemadam Budi", "budi@damkar.surabaya.id", "budi", "budi123", "08333456789",
                "PK-SBY-903", "Anggota");
        budi.setAssignedStation(fireStations.get(2));
        users.put("budi", budi);

        User u1 = new User("Warga Biasa", "warga@email.com", "user", "user123", "08444567890", "user");
        users.put(u1.getUsername(), u1);

        // Initialize roadNetwork Graph
        roadNetwork = FireStationGraph.createSurabayaNetwork(fireStations);
    }

    // Users (HashMap)

    public static HashMap<String, Account> getUsers() {
        return users;
    }

    public static Account findUser(String username) {
        return users.get(username);
    }

    public static void addUser(Account account) {
        users.put(account.getUsername(), account);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    // Session

    public static Account getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Account a) {
        currentUser = a;
    }

    public static void logout() {
        currentUser = null;
    }

    //  Incidents (ArrayList + PriorityQueue)

    public static ArrayList<Incident> getAllIncidents() {
        return allIncidents;
    }

    public static Incident getLastIncident() {
        return allIncidents.isEmpty() ? null : allIncidents.get(allIncidents.size() - 1);
    }

    public static void addIncident(Incident inc) {
        inc.calculatePriorityScore(
                priorityRules.getVictimWeight(),
                priorityRules.getAreaWeight(),
                priorityRules.getIntensityWeight());
        allIncidents.add(inc);
        if (inc.getStatus() != IncidentStatus.RESOLVED)
            incidentQueue.offer(inc);
    }

    public static PriorityQueue<Incident> getIncidentQueue() {
        return incidentQueue;
    }

    public static void rebuildQueue() {
        incidentQueue.clear();
        for (Incident inc : allIncidents) {
            if (inc.getStatus() != IncidentStatus.RESOLVED) {
                inc.calculatePriorityScore(
                        priorityRules.getVictimWeight(),
                        priorityRules.getAreaWeight(),
                        priorityRules.getIntensityWeight());
                incidentQueue.offer(inc);
            }
        }
    }

    // History (ArrayList)

    public static ArrayList<ReportHistory> getReportHistory() {
        return reportHistory;
    }

    public static void addReportHistory(ReportHistory rh) {
        reportHistory.add(rh);
    }

    // Station & Rules

    public static ArrayList<FireStation> getFireStations() {
        return fireStations;
    }

    public static FireStationGraph getRoadNetwork() {
        return roadNetwork;
    }

    public static FireStation getCurrentAdminStation() {
        if (currentUser instanceof Admin) {
            return ((Admin) currentUser).getAssignedStation();
        }
        return fireStations.isEmpty() ? null : fireStations.get(0);
    }

    public static FireStation getFireStation() {
        return getCurrentAdminStation();
    }

    public static DispatchPriorityRules getPriorityRules() {
        return priorityRules;
    }
}
