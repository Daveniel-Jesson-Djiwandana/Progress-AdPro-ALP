package service;

import database.Database;
import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

public class IncidentService {

    // dipakai hanya untuk randomize
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
        if (location.isBlank())
            return "Koordinat belum dipilih.";
        if (description.isBlank())
            description = "Kebakaran dilaporkan oleh warga.";
        if (intensity < 1 || intensity > 10)
            return "Intensitas harus antara 1-10.";
        if (victims < 0 || area < 0)
            return "Nilai tidak boleh negatif.";

        Structure structure = new Structure(location, area, victims);
        Incident incident = new Incident(structure, IncidentSeverity.UNDETERMINED, description, intensity, reportedBy);
        Database.addIncident(incident);
        // Auto-dispatch dimatikan: dispatch dilakukan manual oleh admin pos
        return null;
    }

    public static Incident randomizeIncident(String label, Runnable onGeocodeComplete) {
        Random rng = new Random();
        String locName = RANDOM_LOCATIONS[rng.nextInt(RANDOM_LOCATIONS.length)];
        String desc = RANDOM_DESCS[rng.nextInt(RANDOM_DESCS.length)];
        IncidentSeverity sev = IncidentSeverity.UNDETERMINED;
        int victims = rng.nextInt(11);
        int area = 20 + rng.nextInt(481);
        int intensity = 1 + rng.nextInt(10);

        // Generate random Lat/Lon within Surabaya geographic boundaries
        double lat = -7.34 + rng.nextDouble() * 0.12; // range [-7.34, -7.22]
        double lon = 112.63 + rng.nextDouble() * 0.18; // range [112.63, 112.81]

        // Jika user adalah admin pos tertentu (bukan super admin), generate koordinat di dekat posnya
        FireStation myStation = Database.getCurrentAdminStation();
        boolean superAdmin = false;
        Account acc = Database.getCurrentUser();
        if (acc instanceof Admin) {
            superAdmin = "Super".equalsIgnoreCase(((Admin) acc).getRank());
        }

        if (myStation != null && !superAdmin) {
            double stationLat = myStation.getLatitude();
            double stationLon = myStation.getLongitude();
            // Generate random offset within ~1.5km (approx 0.012 degrees)
            lat = stationLat - 0.01 + rng.nextDouble() * 0.02;
            lon = stationLon - 0.01 + rng.nextDouble() * 0.02;
        }

        final double finalLat = lat;
        final double finalLon = lon;

        String initialLoc = String.format("Lat: %.5f, Lon: %.5f (Mencari alamat...)", finalLat, finalLon);

        Structure structure = new Structure(initialLoc, area, victims);
        Incident incident = new Incident(structure, sev, desc, intensity, label);
        Database.addIncident(incident);
        // Auto-dispatch dimatikan: dispatch dilakukan manual oleh admin pos

        // Perform Nominatim reverse geocoding asynchronously
        new Thread(() -> {
            try {
                String urlStr = String.format(
                        "https://nominatim.openstreetmap.org/reverse?lat=%.7f&lon=%.7f&format=json&addressdetails=1",
                        finalLat, finalLon);
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "SiagaKebakaran/1.0 (fire-reporting-app)");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5004440);

                int respCode = conn.getResponseCode();
                if (respCode == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    String displayName = extractJsonString(response.toString(), "display_name");
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        String finalAddr = displayName.trim();
                        incident.getStructure()
                                .setLocation(String.format("Lat: %.5f, Lon: %.5f (%s)", finalLat, finalLon, finalAddr));
                    } else {
                        incident.getStructure().setLocation(String.format("Lat: %.5f, Lon: %.5f", finalLat, finalLon));
                    }
                } else {
                    incident.getStructure().setLocation(String.format("Lat: %.5f, Lon: %.5f", finalLat, finalLon));
                }
            } catch (Exception e) {
                incident.getStructure().setLocation(String.format("Lat: %.5f, Lon: %.5f", finalLat, finalLon));
            } finally {
                if (onGeocodeComplete != null) {
                    javax.swing.SwingUtilities.invokeLater(onGeocodeComplete);
                }
            }
        }, "RandomizerGeocodeThread").start();

        return incident;
    }

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1)
            return null;
        start += search.length();

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                if (c == 'u') {
                    if (i + 4 < json.length()) {
                        try {
                            int code = Integer.parseInt(json.substring(i + 1, i + 5), 16);
                            sb.append((char) code);
                            i += 4;
                        } catch (NumberFormatException e) {
                            sb.append("\\u");
                        }
                    } else {
                        sb.append("\\u");
                    }
                } else if (c == 'n') {
                    sb.append('\n');
                } else if (c == 't') {
                    sb.append('\t');
                } else if (c == 'r') {
                    sb.append('\r');
                } else {
                    sb.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Incident autoRandomize() {
        return randomizeIncident("Sistem Otomatis (Simulasi)", null);
    }

    /**
     * Cari pos pemadam terdekat dari sebuah insiden menggunakan Dijkstra/Haversine.
     * Digunakan oleh panel status & dispatch untuk filter per wilayah pos.
     * Mengembalikan FireStation terdekat, atau null jika koordinat tidak tersedia.
     */
    public static FireStation getClosestStation(Incident inc) {
        double[] coords = FireStationGraph.parseGpsCoord(inc.getLocation());
        if (coords == null) return null;

        FireStationGraph graph = Database.getRoadNetwork();
        FireStationGraph.Node incidentNode = new FireStationGraph.Node("TempClosestSvc", coords[0], coords[1]);
        graph.addNode(incidentNode);
        FireStationGraph.Node nearestNode = graph.findClosestNode(coords[0], coords[1]);
        if (nearestNode != null) graph.addEdge("TempClosestSvc", nearestNode.id);
        java.util.Map<FireStationGraph.Node, Double> dists = graph.dijkstra(incidentNode);

        FireStation closest = null;
        double minDist = Double.MAX_VALUE;
        for (FireStation station : Database.getFireStations()) {
            FireStationGraph.Node sNode = new FireStationGraph.Node(
                    station.getName(), station.getLatitude(), station.getLongitude());
            Double d = dists.get(sNode);
            if (d != null) {
                double dVal = d < 999999.0 ? d
                        : FireStationGraph.haversineDistance(coords[0], coords[1],
                                station.getLatitude(), station.getLongitude());
                if (dVal < minDist) {
                    minDist = dVal;
                    closest = station;
                }
            }
        }
        graph.removeNode(incidentNode);
        return closest;
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
            if (inc.getStatus() != IncidentStatus.RESOLVED)
                active.add(inc);
        return active;
    }

    private static class StationDistance {
        FireStation station;
        double distance;

        StationDistance(FireStation s, double d) {
            this.station = s;
            this.distance = d;
        }
    }

    public static void autoDispatch(Incident inc) {
        if (inc.getStatus() == IncidentStatus.RESOLVED || inc.getStatus() == IncidentStatus.DISPATCHED) {
            return;
        }

        int count = inc.getRecommendedTrucks();
        int remaining = count;

        double[] coords = FireStationGraph.parseGpsCoord(inc.getLocation());
        ArrayList<StationDistance> list = new ArrayList<>();

        if (coords != null) {
            FireStationGraph graph = Database.getRoadNetwork();
            FireStationGraph.Node incidentNode = new FireStationGraph.Node("TempAuto", coords[0], coords[1]);
            graph.addNode(incidentNode);
            FireStationGraph.Node closestNode = graph.findClosestNode(coords[0], coords[1]);
            if (closestNode != null) {
                graph.addEdge("TempAuto", closestNode.id);
            }

            Map<FireStationGraph.Node, Double> dists = graph.dijkstra(incidentNode);

            for (FireStation station : Database.getFireStations()) {
                FireStationGraph.Node sNode = new FireStationGraph.Node(station.getName(), station.getLatitude(),
                        station.getLongitude());
                Double d = dists.get(sNode);
                if (d != null && station.getAvailableTruckCount() > 0) {
                    double dVal = d < 999999.0 ? d
                            : FireStationGraph.haversineDistance(coords[0], coords[1], station.getLatitude(),
                                    station.getLongitude());
                    list.add(new StationDistance(station, dVal));
                }
            }
            graph.removeNode(incidentNode);
        } else {
            for (FireStation station : Database.getFireStations()) {
                if (station.getAvailableTruckCount() > 0) {
                    list.add(new StationDistance(station, 0.0));
                }
            }
        }

        // Sort by distance (closest first)
        list.sort(Comparator.comparingDouble(sd -> sd.distance));

        int dispatchedCount = 0;
        for (StationDistance sd : list) {
            if (remaining <= 0)
                break;
            int avail = sd.station.getAvailableTruckCount();
            if (inc.getSeverity() == IncidentSeverity.TRIPLE_RED) {
                avail = Math.max(0, avail - 1); // Enforce 1 guard truck standby
            }
            int take = Math.min(remaining, avail);
            if (take <= 0)
                continue;

            ArrayList<Firetruck> assistTrucks = sd.station.getAvailableTrucks();
            for (int i = 0; i < take; i++) {
                assistTrucks.get(i).setStatus(TruckStatus.DEPLOYED);
            }

            dispatchedCount += take;
            remaining -= take;
        }

        if (dispatchedCount > 0) {
            inc.setStatus(IncidentStatus.DISPATCHED);
            inc.setTrucksAssigned(dispatchedCount);
            inc.startDispatch();
        }
    }
}
