package model;

import java.util.*;
import java.util.regex.*;

public class FireStationGraph {

    private static final Pattern GPS_PAT = Pattern.compile("Lat:\\s*(-?\\d+\\.\\d+),\\s*Lon:\\s*(-?\\d+\\.\\d+)");
    private static final Pattern SIM_PAT = Pattern.compile("\\[(\\d+),(\\d+)\\]");

    public static double[] parseGpsCoord(String loc) {
        if (loc == null)
            return null;

        // parsing jadi array kalau ada lat dan lon
        Matcher mGps = GPS_PAT.matcher(loc);
        if (mGps.find()) {
            try {
                double lat = Double.parseDouble(mGps.group(1));
                double lon = Double.parseDouble(mGps.group(2));
                return new double[] { lat, lon };
            } catch (NumberFormatException ignored) {
            }
        }

        // pure parsing raw lat lon
        String[] parts = loc.split(",");
        if (parts.length >= 2) {
            try {
                String latStr = parts[0].replaceAll("[^0-9.-]", "");
                String lonStr = parts[1].replaceAll("[^0-9.-]", "");
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                    return new double[] { lat, lon };
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // simulasi grid tapi sudah gk dipake
        Matcher mSim = SIM_PAT.matcher(loc);
        if (mSim.find()) {
            try {
                double rx = Double.parseDouble(mSim.group(1));
                double ry = Double.parseDouble(mSim.group(2));
                //Surabaya grid:
                // Lat: -7.20 (y=0) to -7.35 (y=1000)
                // Lon: 112.60 (x=0) to 112.85 (x=1000)
                double lat = -7.20 - (ry / 1000.0) * 0.15;
                double lon = 112.60 + (rx / 1000.0) * 0.25;
                return new double[] { lat, lon };
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }

    public static class Node {
        public String id;
        public double latitude;
        public double longitude;

        public Node(String id, double latitude, double longitude) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Node node = (Node) o;
            return id.equals(node.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static class Edge {
        public Node target;
        public double weight;

        public Edge(Node target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<Node, List<Edge>> adjList = new HashMap<>();

    public void addNode(Node node) {
        nodes.put(node.id, node);
        adjList.putIfAbsent(node, new ArrayList<>());
    }

    public void addEdge(String id1, String id2) {
        Node n1 = nodes.get(id1);
        Node n2 = nodes.get(id2);
        if (n1 != null && n2 != null) {
            double distance = haversineDistance(n1.latitude, n1.longitude, n2.latitude, n2.longitude);
            adjList.get(n1).add(new Edge(n2, distance));
            adjList.get(n2).add(new Edge(n1, distance));
        }
    }

    public void removeNode(Node node) {
        nodes.remove(node.id);
        adjList.remove(node);
        for (List<Edge> edges : adjList.values()) {
            edges.removeIf(edge -> edge.target.equals(node));
        }
    }

    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public Node findClosestNode(double lat, double lon) {
        Node closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Node node : adjList.keySet()) {
            double d = haversineDistance(lat, lon, node.latitude, node.longitude);
            if (d < minDistance) {
                minDistance = d;
                closest = node;
            }
        }
        return closest;
    }

    public Map<Node, Double> dijkstra(Node source) {
        Map<Node, Double> distances = new HashMap<>();
        for (Node node : adjList.keySet()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(source, 0.0);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        pq.offer(new NodeDistance(source, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Node u = current.node;
            double distU = current.distance;

            if (distU > distances.get(u))
                continue;

            List<Edge> neighbors = adjList.get(u);
            if (neighbors != null) {
                for (Edge edge : neighbors) {
                    Node v = edge.target;
                    double weight = edge.weight;
                    double newDist = distU + weight;
                    if (newDist < distances.get(v)) {
                        distances.put(v, newDist);
                        pq.offer(new NodeDistance(v, newDist));
                    }
                }
            }
        }
        return distances;
    }

    private static class NodeDistance {
        Node node;
        double distance;

        NodeDistance(Node node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    public static FireStationGraph createSurabayaNetwork(List<FireStation> stations) {
        FireStationGraph graph = new FireStationGraph();

        //tambah node firestations
        for (FireStation s : stations) {
            graph.addNode(new Node(s.getName(), s.getLatitude(), s.getLongitude()));
        }

        //tambah junction utama
        Node iWonokromo = new Node("Wonokromo Junction", -7.30150, 112.73650);
        Node iGubeng = new Node("Gubeng Junction", -7.27250, 112.75380);
        Node iTunjungan = new Node("Tunjungan Junction", -7.25950, 112.73880);
        Node iDarmo = new Node("Darmo Junction", -7.28910, 112.73910);
        Node iKenjeranMerr = new Node("Kenjeran MERR", -7.25580, 112.77250);
        Node iRungkutMerr = new Node("Rungkut MERR", -7.31950, 112.78010);
        Node iWiyungKp = new Node("Wiyung-Karangpilang Junction", -7.31500, 112.69120);
        Node iDemakPerak = new Node("Demak-Perak Junction", -7.24100, 112.72120);

        for (Node i : new Node[] { iWonokromo, iGubeng, iTunjungan, iDarmo, iKenjeranMerr, iRungkutMerr, iWiyungKp,
                iDemakPerak }) {
            graph.addNode(i);
        }

        //connect
        // Bubutan (1) & Pasar Turi (2) -> Tunjungan
        graph.addEdge("Dinas Pemadam Kebakaran & Penyelamatan Surabaya", "Tunjungan Junction");
        graph.addEdge("Dinas Pemadam Kebakaran Kota Surabaya – Pos Pasar Turi/Bubutan", "Tunjungan Junction");
        graph.addEdge("Dinas Pemadam Kebakaran & Penyelamatan Surabaya",
                "Dinas Pemadam Kebakaran Kota Surabaya – Pos Pasar Turi/Bubutan");

        // Perak Barat (3) -> Demak-Perak
        graph.addEdge("Pos Damkar Perak Barat", "Demak-Perak Junction");

        // Tirta 5 Benowo (6) -> Demak-Perak
        graph.addEdge("Pos Damkar Tirta 5 Demak / Benowo", "Demak-Perak Junction");

        // Demak Grudo (4) -> Wonokromo
        graph.addEdge("Pos Damkar 1 Demak Grudo", "Wonokromo Junction");

        // Demak-Perak -> Demak Grudo
        graph.addEdge("Demak-Perak Junction", "Pos Damkar 1 Demak Grudo");

        // Wonokromo -> Darmo
        graph.addEdge("Wonokromo Junction", "Darmo Junction");

        // Wonokromo -> Gubeng
        graph.addEdge("Wonokromo Junction", "Gubeng Junction");

        // Wonokromo -> Wiyung-Karangpilang
        graph.addEdge("Wonokromo Junction", "Wiyung-Karangpilang Junction");

        // Wiyung-Karangpilang -> Wiyung (10)
        graph.addEdge("Wiyung-Karangpilang Junction", "Pos Damkar Wiyung");

        // Wiyung-Karangpilang -> Karangpilang (12)
        graph.addEdge("Wiyung-Karangpilang Junction", "Pos Damkar Karangpilang");

        // Wiyung -> Lakarsantri (13)
        graph.addEdge("Pos Damkar Wiyung", "Pos Damkar Lakarsantri");

        // Darmo -> Tunjungan
        graph.addEdge("Darmo Junction", "Tunjungan Junction");

        // Gubeng -> Tunjungan
        graph.addEdge("Gubeng Junction", "Tunjungan Junction");

        // Gubeng -> Menur (7)
        graph.addEdge("Gubeng Junction", "Pos Damkar Menur");

        // Menur -> Sukolilo (15)
        graph.addEdge("Pos Damkar Menur", "Pos Damkar Sukolilo");

        // Sukolilo -> Keputih (9)
        graph.addEdge("Pos Damkar Sukolilo", "Pos Damkar Keputih");

        // Sukolilo -> Rungkut MERR
        graph.addEdge("Pos Damkar Sukolilo", "Rungkut MERR");

        // Rungkut MERR -> Rungkut (11)
        graph.addEdge("Rungkut MERR", "Pos Damkar Rungkut");

        // Rungkut MERR -> Gunung Anyar (5)
        graph.addEdge("Rungkut MERR", "Pos Damkar Gunung Anyar");

        // Kenjeran MERR -> Sukolilo
        graph.addEdge("Kenjeran MERR", "Pos Damkar Sukolilo");

        // Kenjeran MERR -> Kenjeran (14)
        graph.addEdge("Kenjeran MERR", "Pos Damkar Kenjeran");

        // Kenjeran -> Bulak (8)
        graph.addEdge("Pos Damkar Kenjeran", "Pos Damkar Bulak");

        // Kenjeran -> Tunjungan
        graph.addEdge("Pos Damkar Kenjeran", "Tunjungan Junction");

        return graph;
    }
}
