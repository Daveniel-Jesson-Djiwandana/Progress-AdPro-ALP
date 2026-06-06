package ui.user;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import org.openstreetmap.gui.jmapviewer.tilesources.*;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;

import ui.UITheme;
import database.Database;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.BiConsumer;

/**
 * Panel peta kota Surabaya menggunakan JMapViewer (OpenStreetMap tiles).
 * Mendukung zoom, pan (drag), klik untuk pilih koordinat.
 * Menggantikan CityMapPanel yang berbasis gambar statis.
 */
public class OsmCityMapPanel extends JPanel {

    // Pusat kota Surabaya
    private static final double SURABAYA_LAT = -7.2575;
    private static final double SURABAYA_LON = 112.7521;
    private static final int    DEFAULT_ZOOM = 14;

    private final JMapViewer    map;
    private MapMarkerDot        selectedMarker;
    private double              selectedLat = Double.NaN;
    private double              selectedLon = Double.NaN;

    /** Callback dipanggil saat user klik peta */
    private final BiConsumer<Double, Double> onLocationPicked;

    // Incident display support (for admin dispatch view)
    private final java.util.List<IncidentMarker> incidentMarkers = new java.util.ArrayList<>();
    private java.util.List<model.Incident> currentIncidents = new java.util.ArrayList<>();
    private int highlightedRow = -1;
    private java.util.function.IntConsumer onIncidentClicked;

    // Pos Damkar markers
    private final java.util.List<StationMarker> stationMarkers = new java.util.ArrayList<>();

    // ── Konstruktor ───────────────────────────────────────────────────────────
    public OsmCityMapPanel(BiConsumer<Double, Double> onLocationPicked) {
        this.onLocationPicked = onLocationPicked;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 35, 45));

        map = new JMapViewer();
        map.setTileSource(new OsmTileSource.Mapnik());
        map.setDisplayPosition(new Coordinate(SURABAYA_LAT, SURABAYA_LON), DEFAULT_ZOOM);
        map.setZoomContolsVisible(false); // kita buat sendiri
        map.setScrollWrapEnabled(false);

        // Remove default map controller's listeners to prevent double events/conflict
        for (MouseListener ml : map.getMouseListeners()) {
            if (ml instanceof DefaultMapController) {
                map.removeMouseListener(ml);
            }
        }
        for (MouseMotionListener mml : map.getMouseMotionListeners()) {
            if (mml instanceof DefaultMapController) {
                map.removeMouseMotionListener(mml);
            }
        }
        for (MouseWheelListener mwl : map.getMouseWheelListeners()) {
            if (mwl instanceof DefaultMapController) {
                map.removeMouseWheelListener(mwl);
            }
        }

        // Add a new DefaultMapController that supports panning with left-click
        DefaultMapController controller = new DefaultMapController(map);
        controller.setMovementMouseButton(MouseEvent.BUTTON1);

        // Restrict map panning bounds specifically to Surabaya and snap back if panned outside
        map.addJMVListener(new JMapViewerEventListener() {
            @Override
            public void processCommand(JMVCommandEvent command) {
                if (command.getCommand() == JMVCommandEvent.COMMAND.ZOOM) {
                    if (map.getZoom() < 11) {
                        SwingUtilities.invokeLater(() -> {
                            map.setZoom(11);
                        });
                    }
                }
                
                if (command.getCommand() == JMVCommandEvent.COMMAND.MOVE ||
                    command.getCommand() == JMVCommandEvent.COMMAND.ZOOM) {
                    ICoordinate pos = map.getPosition();
                    if (pos != null) {
                        // Surabaya boundary checks: Lat [-7.48, -7.15], Lon [112.55, 112.88]
                        if (pos.getLat() < -7.48 || pos.getLat() > -7.15 || 
                            pos.getLon() < 112.55 || pos.getLon() > 112.88) {
                            SwingUtilities.invokeLater(() -> {
                                map.setDisplayPosition(new Coordinate(SURABAYA_LAT, SURABAYA_LON), map.getZoom());
                            });
                        }
                    }
                }
            }
        });

        // ── Style atribusi (pojok kanan bawah) ───────────────────────────────
        map.setBackground(new Color(30, 35, 45));

        // ── Klik untuk pilih titik / insiden ──────────────────────────────────
        map.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    // Check if clicked near an incident marker
                    int clickedIdx = hitTestIncident(e.getPoint());
                    if (clickedIdx >= 0) {
                        if (onIncidentClicked != null) {
                            onIncidentClicked.accept(clickedIdx);
                        }
                        return; // Done
                    }

                    if (onLocationPicked != null) {
                        ICoordinate coord = map.getPosition(e.getPoint());
                        if (coord == null) return;

                        double lat = coord.getLat();
                        double lon = coord.getLon();

                        // Surabaya boundary check: Lat [-7.48, -7.15], Lon [112.55, 112.88]
                        if (lat < -7.48 || lat > -7.15 || 
                            lon < 112.55 || lon > 112.88) {
                            return; // Ignore selection outside Surabaya
                        }

                        setSelectedLocation(lat, lon);

                        onLocationPicked.accept(lat, lon);
                    }
                }
            }
        });

        // ── Inisialisasi Pos Damkar di Peta ──────────────────────────────────
        for (model.FireStation station : Database.getFireStations()) {
            Coordinate coord = new Coordinate(station.getLatitude(), station.getLongitude());
            StationMarker marker = new StationMarker(coord, station);
            stationMarkers.add(marker);
            map.addMapMarker(marker);
        }

        // ── Mouse hover tooltip untuk Pos Damkar & Insiden ────────────────────
        map.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean hovered = false;
                for (StationMarker marker : stationMarkers) {
                    Point p = map.getMapPosition(marker.getCoordinate());
                    if (p != null) {
                        int dx = e.getX() - p.x;
                        int dy = e.getY() - p.y;
                        if (dx * dx + dy * dy <= 12 * 12) {
                            map.setToolTipText("<html><body style='font-family:sans-serif; font-size:11px; padding:3px;'>"
                                + "<b>🚒 " + marker.getStation().getName() + "</b><br>"
                                + "Rayon: " + marker.getStation().getRayon() + " ("
                                + (marker.getStation().isInduk() ? "Pos Induk" : "Pos Pembantu") + ")<br>"
                                + "Truk Standby: <b>" + marker.getStation().getAvailableTruckCount() + "</b> unit"
                                + "</body></html>");
                            hovered = true;
                            break;
                        }
                    }
                }
                if (!hovered) {
                    for (IncidentMarker marker : incidentMarkers) {
                        Point p = map.getMapPosition(marker.getCoordinate());
                        if (p != null) {
                            int dx = e.getX() - p.x;
                            int dy = e.getY() - p.y;
                            if (dx * dx + dy * dy <= 15 * 15) {
                                map.setToolTipText("<html><body style='font-family:sans-serif; font-size:11px; padding:3px;'>"
                                    + "<b>🚨 Insiden " + marker.getIncident().getIncidentId() + "</b><br>"
                                    + marker.getIncident().getLocation().replaceAll("\\[.*?\\]","").trim() + "<br>"
                                    + "Status: " + marker.getIncident().getStatus() + "<br>"
                                    + "Keparahan: <font color='#FF5555'><b>" + marker.getIncident().getSeverity() + "</b></font>"
                                    + "</body></html>");
                                hovered = true;
                                break;
                            }
                        }
                    }
                }
                if (!hovered) {
                    map.setToolTipText(null);
                }
            }
        });

        add(map, BorderLayout.CENTER);
    }

    // ── Zoom API (agar ReportIncidentPanel bisa pakai tombol +/-) ─────────────
    public void zoomIn()    { map.zoomIn(); }
    public void zoomOut()   { map.zoomOut(); }
    public void zoomReset() { map.setDisplayPosition(new Coordinate(SURABAYA_LAT, SURABAYA_LON), DEFAULT_ZOOM); }
    public double getZoom() { return map.getZoom(); }

    /** Pindahkan pusat peta ke koordinat tertentu dengan zoom level */
    public void setCenterPosition(double lat, double lon, int zoom) {
        map.setDisplayPosition(new Coordinate(lat, lon), zoom);
    }

    /** Hapus marker pilihan */
    public void clearSelection() {
        if (selectedMarker != null) {
            map.removeMapMarker(selectedMarker);
            selectedMarker = null;
        }
        selectedLat = Double.NaN;
        selectedLon = Double.NaN;
        map.repaint();
    }

    /** Set lokasi terpilih dan pasang marker pilihan secara programmatis */
    public void setSelectedLocation(double lat, double lon) {
        selectedLat = lat;
        selectedLon = lon;

        if (selectedMarker != null) map.removeMapMarker(selectedMarker);
        selectedMarker = new MapMarkerDot(new Coordinate(lat, lon)) {
            @Override
            public void paint(Graphics g, Point position, int radius) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Lingkaran luar (halo minimalis)
                g2.setColor(new Color(UITheme.ACCENT.getRed(), UITheme.ACCENT.getGreen(), UITheme.ACCENT.getBlue(), 60));
                g2.fillOval(position.x - 16, position.y - 16, 32, 32);
                // Titik merah aksen
                g2.setColor(UITheme.ACCENT);
                g2.fillOval(position.x - 7, position.y - 7, 14, 14);
                // Putih tengah
                g2.setColor(Color.WHITE);
                g2.fillOval(position.x - 3, position.y - 3, 6, 6);
                // Label koordinat
                String label = String.format("%.4f, %.4f", lat, lon);
                g2.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(label) + 10, th = fm.getHeight() + 4;
                int tx = position.x - tw / 2;
                int ty = position.y - 26 - th;
                if (ty < 4) ty = position.y + 18;
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(tx, ty, tw, th, 4, 4);
                g2.setColor(UITheme.ACCENT);
                g2.drawRoundRect(tx, ty, tw, th, 4, 4);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.drawString(label, tx + 5, ty + fm.getAscent() + 2);
                g2.dispose();
            }
        };
        map.addMapMarker(selectedMarker);
        map.repaint();
    }

    /** Koordinat terakhir yang dipilih, atau NaN jika belum ada */
    public double getSelectedLat() { return selectedLat; }
    public double getSelectedLon() { return selectedLon; }

    /** Buat JScrollPane kompatibel — JMapViewer sudah self-contained, return wrapper saja */
    public JPanel createScrollPane() {
        // JMapViewer tidak butuh JScrollPane; kembalikan panel ini sendiri
        return this;
    }

    public void setOnIncidentClicked(java.util.function.IntConsumer onIncidentClicked) {
        this.onIncidentClicked = onIncidentClicked;
    }

    private int hitTestIncident(Point clickPoint) {
        for (IncidentMarker marker : incidentMarkers) {
            Point p = map.getMapPosition(marker.getCoordinate());
            if (p != null) {
                int dx = clickPoint.x - p.x;
                int dy = clickPoint.y - p.y;
                int hitRadius = marker.highlighted ? 18 : 13;
                if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                    return marker.getIndex();
                }
            }
        }
        return -1;
    }

    private void rebuildMarkers() {
        // Remove old markers
        for (IncidentMarker marker : incidentMarkers) {
            map.removeMapMarker(marker);
        }
        incidentMarkers.clear();

        // Add new markers
        for (int i = 0; i < currentIncidents.size(); i++) {
            model.Incident inc = currentIncidents.get(i);
            double[] coords = model.FireStationGraph.parseGpsCoord(inc.getLocation());
            if (coords != null) {
                boolean isHighlighted = (i == highlightedRow);
                Coordinate coord = new Coordinate(coords[0], coords[1]);
                IncidentMarker marker = new IncidentMarker(coord, inc, i, isHighlighted);
                incidentMarkers.add(marker);
                map.addMapMarker(marker);
            }
        }
        map.repaint();
    }

    public void setIncidents(java.util.List<model.Incident> list) {
        this.currentIncidents = new java.util.ArrayList<>(list);
        rebuildMarkers();
    }

    public void setHighlightedRow(int row) {
        this.highlightedRow = row;
        rebuildMarkers();
    }

    // Inner class for painting dynamic incident markers
    public static class IncidentMarker extends MapMarkerDot {
        private final model.Incident incident;
        private final int index;
        private final boolean highlighted;

        public IncidentMarker(Coordinate coord, model.Incident incident, int index, boolean highlighted) {
            super(coord);
            this.incident = incident;
            this.index = index;
            this.highlighted = highlighted;
        }

        public model.Incident getIncident() {
            return incident;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public void paint(Graphics g, Point position, int radius) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean sel = highlighted;
            Color col = UITheme.ACCENT; // standard accent color (red/orange)

            if (sel) {
                // halo solid di sekitar titik terpilih
                g2.setColor(UITheme.ACCENT);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(position.x - 18, position.y - 18, 36, 36);
            }

            int r = sel ? 11 : 8;
            g2.setColor(col);
            g2.fillOval(position.x - r, position.y - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(sel ? 2.5f : 1.5f));
            g2.drawOval(position.x - r, position.y - r, r * 2, r * 2);

            // nomor prioritas (P1, P2, ...)
            String lbl = String.valueOf(index + 1);
            Font f = new Font(UITheme.FONT_FAMILY, Font.BOLD, sel ? 11 : 9);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics(f);
            g2.setColor(Color.WHITE);
            g2.drawString(lbl, position.x - fm.stringWidth(lbl)/2, position.y + fm.getAscent()/2 - 1);

            // tooltip nama lokasi jika dipilih
            if (sel) {
                String name = incident.getLocation().replaceAll("\\[.*?\\]","").trim();
                if (name.length() > 28) {
                    name = name.substring(0, 25) + "...";
                }
                Font nf = new Font(UITheme.FONT_FAMILY, Font.BOLD, 11);
                g2.setFont(nf);
                FontMetrics nfm = g2.getFontMetrics(nf);
                int lw = nfm.stringWidth(name) + 12, lh = nfm.getHeight() + 5;
                int lx = position.x - lw / 2;
                int ly = position.y - r - lh - 4;
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(lx, ly, lw, lh, 6, 6);
                g2.setColor(UITheme.ACCENT);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(lx, ly, lw, lh, 6, 6);
                g2.setColor(Color.WHITE);
                g2.drawString(name, lx + 6, ly + nfm.getAscent() + 2);
            }

            g2.dispose();
        }
    }

    // Inner class for painting fire station markers
    public static class StationMarker extends MapMarkerDot {
        private final model.FireStation station;

        public StationMarker(Coordinate coord, model.FireStation station) {
            super(coord);
            this.station = station;
        }

        public model.FireStation getStation() {
            return station;
        }

        @Override
        public void paint(Graphics g, Point position, int radius) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Pos Induk = Indigo/Blue, Pos Pembantu = Slate/Cool Gray
            Color badgeBg = station.isInduk() ? new Color(15, 82, 186) : new Color(100, 110, 120);
            Color borderCol = Color.WHITE;

            // Draw outer shadow
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillOval(position.x - 9, position.y - 9, 18, 18);

            // Draw station badge
            g2.setColor(badgeBg);
            g2.fillOval(position.x - 8, position.y - 8, 16, 16);

            // Draw border
            g2.setColor(borderCol);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(position.x - 8, position.y - 8, 16, 16);

            // Draw 'I' or 'P' in center
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 9));
            String text = station.isInduk() ? "I" : "P";
            FontMetrics fm = g2.getFontMetrics();
            int tx = position.x - fm.stringWidth(text) / 2;
            int ty = position.y + fm.getAscent() / 2 - 1;
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }
}
