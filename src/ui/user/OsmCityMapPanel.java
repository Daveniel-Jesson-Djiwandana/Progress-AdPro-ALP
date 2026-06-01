package ui.user;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import org.openstreetmap.gui.jmapviewer.tilesources.*;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;

import ui.UITheme;

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

    /** Callback dipanggil saat user klik peta: (lat*1e6_as_int, lon*1e6_as_int) */
    private final BiConsumer<Double, Double> onLocationPicked;

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

        // ── Klik untuk pilih titik ────────────────────────────────────────────
        map.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    ICoordinate coord = map.getPosition(e.getPoint());
                    if (coord == null) return;

                    double lat = coord.getLat();
                    double lon = coord.getLon();

                    // Surabaya boundary check: Lat [-7.48, -7.15], Lon [112.55, 112.88]
                    if (lat < -7.48 || lat > -7.15 || 
                        lon < 112.55 || lon > 112.88) {
                        return; // Ignore selection outside Surabaya
                    }

                    selectedLat = lat;
                    selectedLon = lon;

                    // Hapus marker lama, pasang marker baru
                    if (selectedMarker != null) map.removeMapMarker(selectedMarker);
                    selectedMarker = new MapMarkerDot(new Coordinate(lat, lon)) {
                        @Override
                        public void paint(Graphics g, Point position, int radius) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                               RenderingHints.VALUE_ANTIALIAS_ON);
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

                    onLocationPicked.accept(lat, lon);
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

    /** Koordinat terakhir yang dipilih, atau NaN jika belum ada */
    public double getSelectedLat() { return selectedLat; }
    public double getSelectedLon() { return selectedLon; }

    /** Buat JScrollPane kompatibel — JMapViewer sudah self-contained, return wrapper saja */
    public JPanel createScrollPane() {
        // JMapViewer tidak butuh JScrollPane; kembalikan panel ini sendiri
        return this;
    }
}
