package database;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Central in-memory data store.
 *
 * Data Structures:
 *  - HashMap<String, Account>    : O(1) user lookup by username (login/register)
 *  - ArrayList<Incident>         : All incidents + resolved history
 *  - ArrayList<ReportHistory>    : Archived resolved reports
 *  - PriorityQueue<Incident>     : Dispatch queue (highest priority first)
 */
public class Database {

    // HashMap — O(1) lookup by username
    private static final HashMap<String, Account> users = new HashMap<>();

    // ArrayList — sequential incident list and history
    private static final ArrayList<Incident>      allIncidents  = new ArrayList<>();
    private static final ArrayList<ReportHistory> reportHistory = new ArrayList<>();

    // PriorityQueue — max-heap by Incident.compareTo
    private static final PriorityQueue<Incident>  incidentQueue = new PriorityQueue<>();

    private static FireStation         fireStation   = null;
    private static DispatchPriorityRules priorityRules = new DispatchPriorityRules();
    private static Account             currentUser   = null;

    static { initDemoData(); }

    // ── Init ─────────────────────────────────────────────────────────────────

    private static void initDemoData() {
        fireStation = new FireStation("Dinas Pemadam Kebakaran Kota Surabaya", "Jl. Pasar Turi No. 21, Bubutan, Surabaya");

        Admin a1 = new Admin("Kepala Regu A", "admin@damkar.surabaya.id", "admin",  "admin123", "08111234567", "PK-SBY-001", "Kepala Regu");
        Admin a2 = new Admin("Sersan Ahmad",  "ahmad@damkar.surabaya.id", "ahmad",  "ahmad123", "08222345678", "PK-SBY-002", "Sersan");
        Admin a3 = new Admin("Pemadam Budi",  "budi@damkar.surabaya.id",  "budi",   "budi123",  "08333456789", "PK-SBY-003", "Anggota");
        for (Admin a : new Admin[]{a1, a2, a3}) { users.put(a.getUsername(), a); fireStation.addEmployee(a); }

        User u1 = new User("Warga Biasa", "warga@email.com", "user", "user123", "08444567890", "user");
        users.put(u1.getUsername(), u1);

        Firetruck t1 = new Firetruck("PMK-01", "B 1234 PMK", 5000, 5000, 90);
        Firetruck t2 = new Firetruck("PMK-02", "B 5678 PMK", 5000, 4500, 75);
        Firetruck t3 = new Firetruck("PMK-03", "B 9012 PMK", 3000, 3000, 100);
        Firetruck t4 = new Firetruck("PMK-04", "B 3456 PMK", 3000, 2800, 60);
        t1.addCrewMember(a1); t2.addCrewMember(a2); t3.addCrewMember(a3);
        for (Firetruck t : new Firetruck[]{t1,t2,t3,t4}) fireStation.addFiretruck(t);
    }

    // ── Users (HashMap) ───────────────────────────────────────────────────────

    public static HashMap<String, Account> getUsers()   { return users; }
    public static Account  findUser(String username)    { return users.get(username); }
    public static void     addUser(Account account)     { users.put(account.getUsername(), account); }
    public static boolean  userExists(String username)  { return users.containsKey(username); }

    // ── Session ───────────────────────────────────────────────────────────────

    public static Account getCurrentUser()              { return currentUser; }
    public static void    setCurrentUser(Account a)     { currentUser = a; }
    public static void    logout()                      { currentUser = null; }

    // ── Incidents (ArrayList + PriorityQueue) ─────────────────────────────────

    public static ArrayList<Incident> getAllIncidents()  { return allIncidents; }

    public static void addIncident(Incident inc) {
        inc.calculatePriorityScore(
            priorityRules.getVictimWeight(),
            priorityRules.getAreaWeight(),
            priorityRules.getIntensityWeight()
        );
        allIncidents.add(inc);
        if (inc.getStatus() != IncidentStatus.RESOLVED) incidentQueue.offer(inc);
    }

    public static PriorityQueue<Incident> getIncidentQueue()  { return incidentQueue; }

    public static void rebuildQueue() {
        incidentQueue.clear();
        for (Incident inc : allIncidents) {
            if (inc.getStatus() != IncidentStatus.RESOLVED) {
                inc.calculatePriorityScore(
                    priorityRules.getVictimWeight(),
                    priorityRules.getAreaWeight(),
                    priorityRules.getIntensityWeight()
                );
                incidentQueue.offer(inc);
            }
        }
    }

    // ── History (ArrayList) ───────────────────────────────────────────────────

    public static ArrayList<ReportHistory> getReportHistory()   { return reportHistory; }
    public static void addReportHistory(ReportHistory rh)       { reportHistory.add(rh); }

    // ── Station & Rules ───────────────────────────────────────────────────────

    public static FireStation          getFireStation()    { return fireStation; }
    public static DispatchPriorityRules getPriorityRules() { return priorityRules; }
}
