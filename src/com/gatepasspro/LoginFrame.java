package com.gatepasspro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.UUID;

class LoginFrame extends JFrame {
    private final AppStore store;
    private final JTextField username = Theme.textField();
    private final JPasswordField password = Theme.passwordField();

    LoginFrame(AppStore store) {
        this.store = store;
        setTitle("GatePassPro - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 560));
        build();
    }

    private void build() {
        JPanel root = new JPanel(new GridLayout(1,2));
        root.setBackground(Theme.BG);
        setContentPane(root);

        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(Theme.PRIMARY_DARK);
        left.setBorder(new EmptyBorder(40, 40, 40, 40));
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("GatePassPro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 42));
        logo.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Modern Gate Pass & Schedule Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        subtitle.setForeground(new Color(219, 234, 254));
        JTextArea info = new JTextArea("Issue gate passes, verify returned invoices, check old schedules, and manage users in one clean desktop system.");
        info.setLineWrap(true); info.setWrapStyleWord(true); info.setEditable(false); info.setOpaque(false);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 15)); info.setForeground(new Color(219,234,254));
        brand.add(logo); brand.add(Box.createVerticalStrut(12)); brand.add(subtitle); brand.add(Box.createVerticalStrut(28)); brand.add(info);
        left.add(brand);
        root.add(left);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.BG);
        JPanel card = Theme.card();
        card.setPreferredSize(new Dimension(390, 430));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Welcome Back"); title.setFont(Theme.titleFont()); title.setForeground(Theme.TEXT); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hint = new JLabel("Login to continue"); hint.setFont(Theme.bodyFont()); hint.setForeground(Theme.MUTED); hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title); card.add(Box.createVerticalStrut(6)); card.add(hint); card.add(Box.createVerticalStrut(25));
        addField(card, "Username", username);
        addField(card, "Password", password);
        JButton login = Theme.button("Login"); login.setAlignmentX(Component.LEFT_ALIGNMENT); login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton register = Theme.secondaryButton("Create New Account"); register.setAlignmentX(Component.LEFT_ALIGNMENT); register.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JLabel admin = new JLabel("Default admin: admin / admin123"); admin.setFont(Theme.smallFont()); admin.setForeground(Theme.MUTED); admin.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(login); card.add(Box.createVerticalStrut(10)); card.add(register); card.add(Box.createVerticalStrut(16)); card.add(admin);
        right.add(card);
        root.add(right);
        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> showRegister());
        getRootPane().setDefaultButton(login);
    }

    private void addField(JPanel card, String label, JComponent field) {
        JLabel l = Theme.label(label); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT); field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(l); card.add(Box.createVerticalStrut(7)); card.add(field); card.add(Box.createVerticalStrut(16));
    }

    private void doLogin() {
        String u = username.getText();
        String p = new String(password.getPassword());
        store.login(u, p).ifPresentOrElse(user -> {
            dispose();
            new MainFrame(store, user).setVisible(true);
        }, () -> JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE));
    }

    private void showRegister() {
        JDialog d = new JDialog(this, "Create Account", true);
        d.setSize(430, 470); d.setLocationRelativeTo(this);
        JPanel p = Theme.card(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JTextField fullName = Theme.textField(); JTextField user = Theme.textField(); JPasswordField pass = Theme.passwordField(); JTextField phone = Theme.textField();
        JComboBox<String> role = new JComboBox<>(new String[]{"Gate Officer", "Schedule Officer", "Viewer"});
        role.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); role.setFont(Theme.bodyFont());
        JLabel title = new JLabel("Create User Account"); title.setFont(Theme.h2Font()); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title); p.add(Box.createVerticalStrut(18));
        addField(p, "Full Name", fullName); addField(p, "Username", user); addField(p, "Password", pass); addField(p, "Phone", phone);
        JLabel rl = Theme.label("Role"); rl.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(rl); p.add(Box.createVerticalStrut(7)); p.add(role); p.add(Box.createVerticalStrut(18));
        JButton create = Theme.button("Create Account"); create.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44)); create.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(create);
        d.setContentPane(p);
        create.addActionListener(e -> {
            if (fullName.getText().isBlank() || user.getText().isBlank() || new String(pass.getPassword()).isBlank()) {
                JOptionPane.showMessageDialog(d, "Please fill required fields."); return;
            }
            if (store.usernameExists(user.getText())) { JOptionPane.showMessageDialog(d, "Username already exists."); return; }
            store.addUser(new User(UUID.randomUUID().toString(), fullName.getText().trim(), user.getText().trim(), new String(pass.getPassword()), role.getSelectedItem().toString(), phone.getText().trim()));
            JOptionPane.showMessageDialog(d, "Account created successfully. You can login now.");
            d.dispose();
        });
        d.setVisible(true);
    }
}
