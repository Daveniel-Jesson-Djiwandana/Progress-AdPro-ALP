package ui.user;

import ui.UITheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.function.BiConsumer;

/**
 * Peta kota yang bisa di-scroll, di-zoom, dan di-drag untuk pan.
 * Zoom: Ctrl+Scroll atau tombol +/-.
 * Pan: Klik-tahan dan geser (drag).
 * Pilih lokasi: klik biasa (tanpa drag).
 */
public class CityMapPanel extends JPanel {

    private BufferedImage mapImage;
    private Point         clicked;
    private final BiConsumer<Integer, Integer> onLocationPicked;

    private double zoom       = 1.0;
    private static final double ZOOM_MIN  = 0.5;
    private static final double ZOOM_MAX  = 4.0;
    private static final double ZOOM_STEP = 0.15;

    private int imgW = 1000, imgH = 1000;

    // drag-to-pan state
    private Point dragStart    = null;
    private Point vpAtDragStart = null;
    private boolean dragging   = false;
    private static final int DRAG_THRESHOLD = 5; // piksel minimum sebelum dianggap drag

    public CityMapPanel(BiConsumer<Integer, Integer> onLocationPicked) {
        this.onLocationPicked = onLocationPicked;
        setBackground(new Color(30, 35, 45));
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        try {
            InputStream is = getClass().getResourceAsStream("citymap.png");
            if (is != null) {
                mapImage = ImageIO.read(is);
                imgW = mapImage.getWidth();
                imgH = mapImage.getHeight();
            }
        } catch (Exception e) {
            mapImage = null;
        }

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStart = e.getPoint();
                    JScrollPane sp = getScrollPane();
                    vpAtDragStart = sp != null ? sp.getViewport().getViewPosition() : new Point(0, 0);
                    dragging = false;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;
                int dx = e.getX() - dragStart.x;
                int dy = e.getY() - dragStart.y;
                if (!dragging && (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD)) {
                    dragging = true;
                }
                if (dragging) {
                    JScrollPane sp = getScrollPane();
                    if (sp != null) {
                        int nx = vpAtDragStart.x - dx;
                        int ny = vpAtDragStart.y - dy;
                        sp.getViewport().setViewPosition(new Point(
                            Math.max(0, nx), Math.max(0, ny)));
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!dragging && dragStart != null && SwingUtilities.isLeftMouseButton(e)) {
                    // Ini klik biasa, bukan drag — pilih lokasi
                    clicked = e.getPoint();
                    onLocationPicked.accept(toMapX(e.getX()), toMapY(e.getY()));
                    repaint();
                }
                dragStart = null;
                vpAtDragStart = null;
                dragging = false;
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        // Ctrl+Scroll untuk zoom
        addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                double oldZoom = zoom;
                if (e.getWheelRotation() < 0) zoom = Math.min(ZOOM_MAX, zoom + ZOOM_STEP);
                else                           zoom = Math.max(ZOOM_MIN, zoom - ZOOM_STEP);
                if (zoom != oldZoom) {
                    updatePreferredSize();
                    JScrollPane sp = getScrollPane();
                    if (sp != null) {
                        Point vp = sp.getViewport().getViewPosition();
                        double ratio = zoom / oldZoom;
                        int newX = (int)(e.getX() * ratio) - (sp.getViewport().getWidth()  / 2);
                        int newY = (int)(e.getY() * ratio) - (sp.getViewport().getHeight() / 2);
                        sp.getViewport().setViewPosition(new Point(
                            Math.max(0, newX), Math.max(0, newY)));
                    }
                    revalidate();
                    repaint();
                }
            }
        });
    }

    // ── Zoom API ──────────────────────────────────────────────────────────────
    public void zoomIn()    { setZoom(zoom + ZOOM_STEP); }
    public void zoomOut()   { setZoom(zoom - ZOOM_STEP); }
    public void zoomReset() { setZoom(1.0); }

    public void setZoom(double z) {
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, z));
        updatePreferredSize();
        revalidate();
        repaint();
    }

    public double getZoom() { return zoom; }

    private void updatePreferredSize() {
        setPreferredSize(new Dimension(
            (int)(imgW * zoom),
            (int)(imgH * zoom)));
    }

    // ── JScrollPane helper ────────────────────────────────────────────────────
    private JScrollPane getScrollPane() {
        Container p = getParent();
        if (p instanceof JViewport) {
            Container p2 = p.getParent();
            if (p2 instanceof JScrollPane) return (JScrollPane) p2;
        }
        return null;
    }

    public JScrollPane createScrollPane() {
        updatePreferredSize();
        JScrollPane sp = new JScrollPane(this);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getHorizontalScrollBar().setUnitIncrement(20);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        return sp;
    }

    // ── Koordinat ─────────────────────────────────────────────────────────────
    private int toMapX(int sx) { return (int)(sx / zoom); }
    private int toMapY(int sy) { return (int)(sy / zoom); }

    public int toScreenX(int mx) { return (int)(mx * zoom); }
    public int toScreenY(int my) { return (int)(my * zoom); }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int drawW = (int)(imgW * zoom);
        int drawH = (int)(imgH * zoom);
        // isi seluruh panel agar tidak ada frame hitam di tepi
        int panelW = Math.max(drawW, getWidth());
        int panelH = Math.max(drawH, getHeight());

        if (mapImage != null) {
            // tile/stretch background warna peta agar tidak ada gap
            g2.setColor(new Color(30, 35, 45));
            g2.fillRect(0, 0, panelW, panelH);
            g2.drawImage(mapImage, 0, 0, drawW, drawH, null);
        } else {
            g2.setColor(new Color(40, 50, 65));
            g2.fillRect(0, 0, panelW, panelH);
            g2.setColor(UITheme.TEXT_SECONDARY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("citymap.png tidak ditemukan", 20, drawH / 2);
        }

        if (clicked != null) {
            int cx = clicked.x, cy = clicked.y;
            g2.setColor(new Color(239, 68, 68, 50));
            g2.fillOval(cx - 22, cy - 22, 44, 44);
            g2.setColor(UITheme.ACCENT_RED);
            g2.fillOval(cx - 9, cy - 9, 18, 18);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - 3, cy - 3, 6, 6);

            String lbl = toMapX(cx) + " , " + toMapY(cy);
            Font f = new Font("SansSerif", Font.BOLD, 11);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics(f);
            int tw = fm.stringWidth(lbl) + 12, th = fm.getHeight() + 6;
            int tx = Math.max(4, Math.min(cx - tw/2, drawW - tw - 4));
            int ty = (cy - 25 - th < 4) ? cy + 25 : cy - 25 - th;
            g2.setColor(new Color(10, 10, 20, 200));
            g2.fillRoundRect(tx, ty, tw, th, 6, 6);
            g2.setColor(UITheme.ACCENT_RED);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(tx, ty, tw, th, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(lbl, tx + 6, ty + fm.getAscent() + 3);
        }

        drawZoomHint(g2, drawW, drawH);
        g2.dispose();
    }
    private void drawZoomHint(Graphics2D g2, int w, int h) {
        String hint = String.format("Ctrl+Scroll=Zoom | Drag=Geser  %.0f%%", zoom * 100);
        Font f = new Font("SansSerif", Font.PLAIN, 10);
        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics(f);
        int tw = fm.stringWidth(hint) + 10, th = fm.getHeight() + 4;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(w - tw - 6, h - th - 6, tw, th, 6, 6);
        g2.setColor(new Color(180, 180, 180));
        g2.drawString(hint, w - tw - 1, h - fm.getDescent() - 7);
    }

    public void clearSelection() {
        clicked = null;
        repaint();
    }
}
