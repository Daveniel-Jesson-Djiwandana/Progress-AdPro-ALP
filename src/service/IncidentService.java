package service;

import database.Database;
import model.*;

import java.util.ArrayList;
import java.util.Random;

public class IncidentService {

    // ── Surabaya locations with map pixel coordinates [x, y] (on 800x600 canvas) ──
    public static final Object[][] SURABAYA_SPOTS = {
        // { "Nama Lokasi", pixelX, pixelY }
        { "Jl. Tunjungan No. 5, Genteng",           400, 290 },
        { "Pasar Turi, Jl. Semarang, Bubutan",       300, 240 },
        { "Tunjungan Plaza, Jl. Basuki Rahmat",      405, 295 },
        { "Jl. Raya Darmo No. 23, Wonokromo",        360, 360 },
        { "Terminal Purabaya, Bungurasih",            390, 490 },
        { "Pelabuhan Tanjung Perak, Krembangan",     320, 165 },
        { "Jl. Pemuda No. 17, Genteng",              430, 275 },
        { "Pasar Keputran, Wonokromo",               370, 340 },
        { "Jl. Raya Gubeng No. 54, Gubeng",         470, 285 },
        { "Galaxy Mall, Jl. Dharmahusada",           530, 250 },
        { "ITS Kampus Sukolilo, Sukolilo",           590, 290 },
        { "UNAIR Kampus A, Jl. Airlangga",           455, 255 },
        { "RS. Dr. Soetomo, Jl. Mayjen Prof. Moestopo", 470, 245 },
        { "Jl. Kenjeran No. 88, Bulak",              560, 185 },
        { "Kawasan Industri SIER, Rungkut",          580, 380 },
        { "Jl. Raya Wonokromo, Wonokromo",           360, 380 },
        { "Sidoarjo – Perbatasan Wonocolo",          420, 480 },
        { "Jl. Dukuh Kupang No. 10, Dukuh Pakis",   270, 340 },
        { "Masjid Agung Al-Akbar, Pagesangan",       340, 430 },
        { "Taman Bungkul, Jl. Raya Darmo",           370, 355 },
    };

    private static final String[] DESCRIPTIONS = {
        "Kebakaran berasal dari korsleting listrik di lantai dasar.",
        "Api menyebar dari dapur dan sudah merambat ke ruangan lain.",
        "Kebakaran akibat ledakan tabung gas, asap tebal memenuhi area.",
        "Api terlihat dari jendela lantai dua, penghuni masih ada di dalam.",
        "Kebakaran kecil di gudang bahan kimia, berpotensi meluas.",
        "Sumber api tidak diketahui, sudah menyebar ke dua ruangan.",
        "Kebakaran merambat ke permukiman warga akibat angin kencang.",
        "Api berasal dari instalasi listrik yang sudah tua dan tidak terawat.",
        "Kebakaran di area penyimpanan bahan bakar, potensi ledakan tinggi.",
    };

    /** Report a new incident — creates Structure then Incident. */
    public static String reportIncident(String location, IncidentSeverity severity,
                                        String description, int victims,
                                        int area, int intensity, String reportedBy) {
        if (location.isBlank() || description.isBlank()) return "Lokasi dan deskripsi wajib diisi.";
        if (intensity < 1 || intensity > 10)             return "Intensitas harus antara 1–10.";
        if (victims < 0 || area < 0)                     return "Nilai tidak boleh negatif.";

        Structure structure = new Structure(location, area, victims);
        Incident  incident  = new Incident(structure, severity, description, intensity, reportedBy);
        Database.addIncident(incident);
        return null;
    }

    /** Generate a random Surabaya incident for drill simulation. */
    public static Incident randomizeIncident(String label) {
        Random rng = new Random();
        Object[] spot = SURABAYA_SPOTS[rng.nextInt(SURABAYA_SPOTS.length)];
        String loc  = (String) spot[0];
        String desc = DESCRIPTIONS[rng.nextInt(DESCRIPTIONS.length)];
        IncidentSeverity[] sevs = IncidentSeverity.values();
        IncidentSeverity sev    = sevs[rng.nextInt(sevs.length)];
        int victims   = rng.nextInt(11);
        int area      = 20 + rng.nextInt(481);
        int intensity = 1 + rng.nextInt(10);

        Structure structure = new Structure(loc, area, victims);
        Incident  incident  = new Incident(structure, sev, desc, intensity, label);
        Database.addIncident(incident);
        return incident;
    }

    /** Auto-trigger: creates incident with "Sistem Otomatis" tag */
    public static Incident autoRandomize() {
        return randomizeIncident("Sistem Otomatis (Simulasi)");
    }

    /** Resolve an incident and archive it to history. */
    public static void resolveIncident(Incident incident, String resolvedBy,
                                       int trucksDeployed, String notes) {
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setDispatchProgress(100);
        Database.getIncidentQueue().remove(incident);
        Database.addReportHistory(new ReportHistory(incident, resolvedBy, trucksDeployed, notes));
    }

    public static ArrayList<Incident> getActiveIncidents() {
        ArrayList<Incident> active = new ArrayList<>();
        for (Incident inc : Database.getAllIncidents())
            if (inc.getStatus() != IncidentStatus.RESOLVED) active.add(inc);
        return active;
    }

    /** Get pixel X for a location name (for map rendering) */
    public static int getMapX(String location) {
        for (Object[] spot : SURABAYA_SPOTS) {
            if (location.equals(spot[0])) return (int) spot[1];
        }
        return 400; // default center
    }

    /** Get pixel Y for a location name (for map rendering) */
    public static int getMapY(String location) {
        for (Object[] spot : SURABAYA_SPOTS) {
            if (location.equals(spot[0])) return (int) spot[2];
        }
        return 300; // default center
    }
}
