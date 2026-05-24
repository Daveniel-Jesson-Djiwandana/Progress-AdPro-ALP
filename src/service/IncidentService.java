package service;

import database.Database;
import model.*;

import java.util.ArrayList;
import java.util.Random;

public class IncidentService {

    // dipakai hanya untuk randomize insiden simulasi (AutoFirePanel)
    private static final String[] RANDOM_LOCATIONS = {
        "Jl. Tunjungan, Genteng",
        "Pasar Turi, Bubutan",
        "Jl. Raya Darmo, Wonokromo",
        "Terminal Purabaya, Bungurasih",
        "Pelabuhan Tanjung Perak",
        "Jl. Pemuda, Genteng",
        "Jl. Raya Gubeng, Gubeng",
        "Galaxy Mall, Dharmahusada",
        "ITS Sukolilo",
        "UNAIR Kampus A",
        "RS Dr. Soetomo",
        "Jl. Kenjeran, Bulak",
        "Kawasan Industri SIER, Rungkut",
        "Jl. Dukuh Kupang, Dukuh Pakis",
        "Masjid Al-Akbar, Pagesangan",
        "Taman Bungkul, Jl. Raya Darmo",
    };

    private static final String[] RANDOM_DESCS = {
        "Kebakaran dari korsleting listrik di lantai dasar.",
        "Api menyebar dari dapur ke ruangan lain.",
        "Ledakan tabung gas, asap tebal memenuhi area.",
        "Api terlihat dari jendela lantai dua, penghuni masih ada.",
        "Kebakaran kecil di gudang bahan kimia, berpotensi meluas.",
        "Sumber api tidak diketahui, sudah ke dua ruangan.",
        "Api merambat ke permukiman akibat angin kencang.",
        "Instalasi listrik tua terbakar.",
        "Kebakaran di area penyimpanan bahan bakar.",
    };

    public static String reportIncident(String location, IncidentSeverity severity,
                                        String description, int victims,
                                        int area, int intensity, String reportedBy) {
        if (location.isBlank()) return "Koordinat belum dipilih.";
        if (description.isBlank()) description = "Kebakaran dilaporkan oleh warga.";
        if (intensity < 1 || intensity > 10)             return "Intensitas harus antara 1–10.";
        if (victims < 0 || area < 0)                     return "Nilai tidak boleh negatif.";

        Structure structure = new Structure(location, area, victims);
        Incident  incident  = new Incident(structure, severity, description, intensity, reportedBy);
        Database.addIncident(incident);
        return null;
    }

    public static Incident randomizeIncident(String label) {
        Random rng = new Random();
        String locName = RANDOM_LOCATIONS[rng.nextInt(RANDOM_LOCATIONS.length)];
        String desc    = RANDOM_DESCS[rng.nextInt(RANDOM_DESCS.length)];
        IncidentSeverity sev = IncidentSeverity.values()[rng.nextInt(IncidentSeverity.values().length)];
        int victims   = rng.nextInt(11);
        int area      = 20 + rng.nextInt(481);
        int intensity = 1 + rng.nextInt(10);

        // Tambahkan koordinat acak agar insiden muncul di peta
        int rx = 60 + rng.nextInt(880);
        int ry = 60 + rng.nextInt(880);
        String loc = locName + " [" + rx + "," + ry + "]";

        Structure structure = new Structure(loc, area, victims);
        Incident  incident  = new Incident(structure, sev, desc, intensity, label);
        Database.addIncident(incident);
        return incident;
    }

    public static Incident autoRandomize() {
        return randomizeIncident("Sistem Otomatis (Simulasi)");
    }

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
}
