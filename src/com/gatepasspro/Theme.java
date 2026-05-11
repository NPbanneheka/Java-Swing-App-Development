package com.gatepasspro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

class Theme {
    static final Color BG = new Color(245, 247, 251);
    static final Color CARD = Color.WHITE;
    static final Color PRIMARY = new Color(29, 78, 216);
    static final Color PRIMARY_DARK = new Color(30, 64, 175);
    static final Color TEXT = new Color(17, 24, 39);
    static final Color MUTED = new Color(107, 114, 128);
    static final Color SUCCESS = new Color(22, 163, 74);
    static final Color WARNING = new Color(217, 119, 6);
    static final Color DANGER = new Color(220, 38, 38);

    static Font titleFont() { return new Font("Segoe UI", Font.BOLD, 24); }
    static Font h2Font() { return new Font("Segoe UI", Font.BOLD, 18); }
    static Font bodyFont() { return new Font("Segoe UI", Font.PLAIN, 14); }
    static Font smallFont() { return new Font("Segoe UI", Font.PLAIN, 12); }

    static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(PRIMARY);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static JButton secondaryButton(String text) {
        JButton b = button(text);
        b.setForeground(TEXT);
        b.setBackground(new Color(229, 231, 235));
        return b;
    }

    static JTextField textField() {
        JTextField t = new JTextField();
        t.setFont(bodyFont());
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(209,213,219)), new EmptyBorder(9, 10, 9, 10)));
        return t;
    }

    static JPasswordField passwordField() {
        JPasswordField t = new JPasswordField();
        t.setFont(bodyFont());
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(209,213,219)), new EmptyBorder(9, 10, 9, 10)));
        return t;
    }

    static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT);
        return l;
    }

    static JPanel card() {
        JPanel p = new RoundedPanel(22, CARD);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    static ImageIcon avatar(String path, int size) {
        try {
            Image image;
            if (path != null && !path.isBlank() && new File(path).exists()) {
                image = new ImageIcon(path).getImage();
            } else {
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(219, 234, 254));
                g.fillOval(0, 0, size, size);
                g.setColor(PRIMARY);
                g.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                String s = "U";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(s, (size - fm.stringWidth(s)) / 2, (size + fm.getAscent()) / 2 - 4);
                g.dispose();
                image = img;
            }
            return new ImageIcon(image.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }
}

class RoundedPanel extends JPanel {
    private final int radius;
    private final Color bg;
    RoundedPanel(int radius, Color bg) {
        this.radius = radius;
        this.bg = bg;
        setOpaque(false);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, radius, radius));
        g2.dispose();
        super.paintComponent(g);
    }
}
