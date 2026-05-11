package com.gatepasspro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

class MainFrame extends JFrame {
    private final AppStore store;
    private final User currentUser;
    private final JPanel content = new JPanel(new BorderLayout());
    private JLabel avatar;
    private JLabel nameLabel;

    MainFrame(AppStore store, User currentUser) {
        this.store = store;
        this.currentUser = currentUser;
        setTitle("GatePassPro - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1050, 680));
        build();
        showDashboard();
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        setContentPane(root);

        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(245, 0));
        side.setBackground(new Color(15, 23, 42));
        side.setBorder(new EmptyBorder(24, 18, 24, 18));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel app = new JLabel("GatePassPro");
        app.setFont(new Font("Segoe UI", Font.BOLD, 25));
        app.setForeground(Color.WHITE);
        app.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel role = new JLabel(currentUser.role);
        role.setFont(Theme.smallFont()); role.setForeground(new Color(203,213,225)); role.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(app); side.add(Box.createVerticalStrut(4)); side.add(role); side.add(Box.createVerticalStrut(25));
        addNav(side, "Dashboard", this::showDashboard);
        addNav(side, "Issue Gate Pass", this::showGatePassForm);
        addNav(side, "Verify & Schedule", this::showScheduleForm);
        addNav(side, "Search Records", this::showSearch);
        addNav(side, "Reports", this::showReports);
        addNav(side, "My Profile", this::showProfile);
        if (currentUser.role.equals("Admin")) addNav(side, "Users", this::showUsers);
        side.add(Box.createVerticalGlue());
        JButton logout = navButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame(store).setVisible(true); });
        side.add(logout);
        root.add(side, BorderLayout.WEST);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(14, 22, 14, 22));
        JLabel title = new JLabel("Gate Pass & Schedule Management");
        title.setFont(Theme.h2Font()); title.setForeground(Theme.TEXT);
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); userPanel.setOpaque(false);
        nameLabel = new JLabel(currentUser.fullName); nameLabel.setFont(Theme.bodyFont());
        avatar = new JLabel(Theme.avatar(currentUser.photoPath, 42));
        userPanel.add(nameLabel); userPanel.add(avatar);
        top.add(title, BorderLayout.WEST); top.add(userPanel, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(22, 22, 22, 22));
        root.add(content, BorderLayout.CENTER);
    }

    private void addNav(JPanel side, String text, Runnable action) {
        JButton b = navButton(text);
        b.addActionListener(e -> action.run());
        side.add(b); side.add(Box.createVerticalStrut(8));
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setFocusPainted(false); b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE); b.setBackground(new Color(30, 41, 59));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void setPage(String heading, JComponent page) {
        content.removeAll();
        JPanel wrapper = new JPanel(new BorderLayout(0, 18)); wrapper.setOpaque(false);
        JLabel h = new JLabel(heading); h.setFont(Theme.titleFont()); h.setForeground(Theme.TEXT);
        wrapper.add(h, BorderLayout.NORTH); wrapper.add(page, BorderLayout.CENTER);
        content.add(wrapper, BorderLayout.CENTER);
        content.revalidate(); content.repaint();
    }

    private void showDashboard() {
        JPanel page = new JPanel(new BorderLayout(0, 18)); page.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1,4,16,16)); cards.setOpaque(false);
        cards.add(metric("Gate Passes", String.valueOf(store.gatePasses.size()), Theme.PRIMARY));
        cards.add(metric("Schedules", String.valueOf(store.schedules.size()), Theme.SUCCESS));
        cards.add(metric("Users", String.valueOf(store.users.size()), Theme.WARNING));
        long pending = store.gatePasses.stream().filter(g -> g.status.equals("Issued")).count();
        cards.add(metric("Pending Returns", String.valueOf(pending), Theme.DANGER));
        page.add(cards, BorderLayout.NORTH);
        JTextArea welcome = new JTextArea("Welcome, " + currentUser.fullName + "!\n\nWorkflow:\n1. Issue the gate pass when the vehicle leaves.\n2. When driver returns with sealed invoice, use Verify & Schedule.\n3. Search old gate passes and schedules without changing paper/layout.\n4. Use Reports for quick checking and printing.");
        welcome.setEditable(false); welcome.setFont(new Font("Segoe UI", Font.PLAIN, 16)); welcome.setLineWrap(true); welcome.setWrapStyleWord(true);
        JPanel card = Theme.card(); card.setLayout(new BorderLayout()); card.add(welcome);
        page.add(card, BorderLayout.CENTER);
        setPage("Dashboard", page);
    }

    private JPanel metric(String title, String value, Color color) {
        JPanel p = Theme.card(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title); t.setFont(Theme.bodyFont()); t.setForeground(Theme.MUTED);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 34)); v.setForeground(color);
        p.add(t); p.add(Box.createVerticalStrut(10)); p.add(v); return p;
    }

    private void showGatePassForm() {
        JPanel p = Theme.card(); p.setLayout(new GridBagLayout()); GridBagConstraints c = gbc();
        JTextField vehicle = Theme.textField(), driver = Theme.textField(), phone = Theme.textField(), destination = Theme.textField(), invoice = Theme.textField(), route = Theme.textField();
        JTextArea goods = area(), remarks = area();
        int y=0; y=field(p,c,y,"Vehicle Number",vehicle,"Driver Name",driver); y=field(p,c,y,"Driver Phone",phone,"Destination",destination); y=field(p,c,y,"Invoice Number",invoice,"Route Name",route);
        y=areaField(p,c,y,"Goods Description",goods); y=areaField(p,c,y,"Remarks",remarks);
        JButton save = Theme.button("Issue Gate Pass"); JButton clear = Theme.secondaryButton("Clear");
        c.gridx=0;c.gridy=y;c.gridwidth=2;c.fill=GridBagConstraints.NONE;c.anchor=GridBagConstraints.WEST;p.add(save,c); c.gridx=1;p.add(clear,c);
        save.addActionListener(e -> {
            if (vehicle.getText().isBlank() || driver.getText().isBlank() || invoice.getText().isBlank()) { JOptionPane.showMessageDialog(this,"Vehicle, Driver and Invoice are required."); return; }
            GatePass gp = new GatePass(store.nextGatePassId(), vehicle.getText(), driver.getText(), phone.getText(), destination.getText(), invoice.getText(), route.getText(), goods.getText(), currentUser.fullName, remarks.getText());
            store.gatePasses.add(gp); store.save(); JOptionPane.showMessageDialog(this,"Gate Pass Issued: " + gp.id); showReports();
        });
        clear.addActionListener(e -> { for(Component comp:p.getComponents()) if(comp instanceof JTextField) ((JTextField)comp).setText(""); goods.setText(""); remarks.setText(""); });
        setPage("Issue Gate Pass", new JScrollPane(p));
    }

    private void showScheduleForm() {
        JPanel p = Theme.card(); p.setLayout(new GridBagLayout()); GridBagConstraints c = gbc();
        JTextField gpId = Theme.textField(); JTextField invoice = Theme.textField(); JTextField vehicle = Theme.textField(); JTextField driver = Theme.textField();
        JCheckBox sealed = new JCheckBox("Invoice sealed and returned"); JCheckBox goodsChecked = new JCheckBox("Goods checked");
        JComboBox<String> status = new JComboBox<>(new String[]{"Schedule Issued", "Hold", "Rejected"}); JTextArea remarks = area();
        JButton load = Theme.secondaryButton("Load Gate Pass"); JButton save = Theme.button("Save Verification / Issue Schedule");
        int y=0; c.gridx=0;c.gridy=y;p.add(Theme.label("Gate Pass ID"),c); c.gridx=1;p.add(gpId,c); c.gridx=2;p.add(load,c); y++;
        y=field(p,c,y,"Invoice Number",invoice,"Vehicle Number",vehicle); y=field(p,c,y,"Driver Name",driver,"Status",status);
        c.gridx=0;c.gridy=y;c.gridwidth=3;p.add(sealed,c); y++; c.gridy=y;p.add(goodsChecked,c); y++; y=areaField(p,c,y,"Remarks",remarks);
        c.gridx=0;c.gridy=y;c.gridwidth=3;c.fill=GridBagConstraints.NONE;c.anchor=GridBagConstraints.WEST;p.add(save,c);
        load.addActionListener(e -> store.findGatePass(gpId.getText()).ifPresentOrElse(g -> { invoice.setText(g.invoiceNo); vehicle.setText(g.vehicleNo); driver.setText(g.driverName); }, () -> JOptionPane.showMessageDialog(this,"Gate pass not found.")));
        save.addActionListener(e -> {
            if (gpId.getText().isBlank() || invoice.getText().isBlank()) { JOptionPane.showMessageDialog(this,"Gate Pass ID and Invoice are required."); return; }
            ScheduleRecord sr = new ScheduleRecord(store.nextScheduleId(), gpId.getText(), invoice.getText(), vehicle.getText(), driver.getText(), currentUser.fullName, sealed.isSelected(), goodsChecked.isSelected(), status.getSelectedItem().toString(), remarks.getText());
            store.schedules.add(sr); store.findGatePass(gpId.getText()).ifPresent(g -> g.status = sr.status); store.save(); JOptionPane.showMessageDialog(this,"Schedule Saved: " + sr.id); showReports();
        });
        setPage("Verify Driver Return & Issue Schedule", new JScrollPane(p));
    }

    private void showSearch() {
        JPanel page = new JPanel(new BorderLayout(0,12)); page.setOpaque(false);
        JPanel bar = Theme.card(); bar.setLayout(new BorderLayout(10,0)); JTextField q = Theme.textField(); JButton search = Theme.button("Search"); bar.add(q,BorderLayout.CENTER); bar.add(search,BorderLayout.EAST);
        JTable table = new JTable(); JScrollPane scroll = new JScrollPane(table);
        Runnable fill = () -> {
            String key = q.getText().toLowerCase(); DefaultTableModel m = new DefaultTableModel(new String[]{"Type","ID","Vehicle","Driver","Invoice","Status","Date"},0);
            for(GatePass g: store.gatePasses) if(matches(key,g.id,g.vehicleNo,g.driverName,g.invoiceNo,g.status)) m.addRow(new Object[]{"Gate Pass",g.id,g.vehicleNo,g.driverName,g.invoiceNo,g.status,g.issueDate});
            for(ScheduleRecord s: store.schedules) if(matches(key,s.id,s.gatePassId,s.vehicleNo,s.driverName,s.invoiceNo,s.status)) m.addRow(new Object[]{"Schedule",s.id,s.vehicleNo,s.driverName,s.invoiceNo,s.status,s.checkedDate});
            table.setModel(m);
        };
        search.addActionListener(e -> fill.run()); fill.run(); page.add(bar,BorderLayout.NORTH); page.add(scroll,BorderLayout.CENTER); setPage("Search Old Gate Passes & Schedules", page);
    }

    private boolean matches(String key, String... vals) { if(key.isBlank()) return true; for(String v: vals) if(v!=null && v.toLowerCase().contains(key)) return true; return false; }

    private void showReports() {
        JPanel page = new JPanel(new BorderLayout(0,12)); page.setOpaque(false);
        JButton print = Theme.secondaryButton("Print Table"); JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bar.setOpaque(false); bar.add(print);
        DefaultTableModel m = new DefaultTableModel(new String[]{"Gate Pass ID","Vehicle","Driver","Invoice","Route","Issued By","Date","Status"},0);
        for(GatePass g: store.gatePasses) m.addRow(new Object[]{g.id,g.vehicleNo,g.driverName,g.invoiceNo,g.routeName,g.issuedBy,g.issueDate,g.status});
        JTable table = new JTable(m); table.setRowHeight(28); print.addActionListener(e -> { try { table.print(); } catch (PrinterException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }});
        page.add(bar,BorderLayout.NORTH); page.add(new JScrollPane(table),BorderLayout.CENTER); setPage("Reports", page);
    }

    private void showProfile() {
        JPanel p = Theme.card(); p.setLayout(new GridBagLayout()); GridBagConstraints c = gbc();
        JLabel photo = new JLabel(Theme.avatar(currentUser.photoPath, 120)); JTextField name = Theme.textField(); JTextField phone = Theme.textField(); JPasswordField pass = Theme.passwordField();
        name.setText(currentUser.fullName); phone.setText(currentUser.phone); pass.setText(currentUser.password);
        JButton choose = Theme.secondaryButton("Upload / Change Photo"); JButton save = Theme.button("Save Profile");
        c.gridx=0;c.gridy=0;c.gridwidth=2;c.anchor=GridBagConstraints.CENTER;p.add(photo,c); c.gridy=1;p.add(choose,c); c.gridwidth=1;c.anchor=GridBagConstraints.WEST;
        int y=2; y=field(p,c,y,"Full Name",name,"Phone",phone); c.gridx=0;c.gridy=y;p.add(Theme.label("Password"),c); c.gridx=1;p.add(pass,c); y++; c.gridx=0;c.gridy=y;c.gridwidth=2;p.add(save,c);
        choose.addActionListener(e -> { JFileChooser fc = new JFileChooser(); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){ try{ File src=fc.getSelectedFile(); String ext=src.getName().contains(".")?src.getName().substring(src.getName().lastIndexOf('.')):".jpg"; File dst=new File("data/profile_photos/"+currentUser.id+ext); Files.copy(src.toPath(),dst.toPath(), StandardCopyOption.REPLACE_EXISTING); currentUser.photoPath=dst.getPath(); store.save(); photo.setIcon(Theme.avatar(currentUser.photoPath,120)); avatar.setIcon(Theme.avatar(currentUser.photoPath,42)); }catch(Exception ex){ JOptionPane.showMessageDialog(this,ex.getMessage()); } }});
        save.addActionListener(e -> { currentUser.fullName=name.getText(); currentUser.phone=phone.getText(); currentUser.password=new String(pass.getPassword()); store.save(); nameLabel.setText(currentUser.fullName); JOptionPane.showMessageDialog(this,"Profile saved."); });
        setPage("My Profile", p);
    }

    private void showUsers() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"Full Name","Username","Role","Phone","Status"},0);
        for(User u: store.users) m.addRow(new Object[]{u.fullName,u.username,u.role,u.phone,u.status});
        JTable table = new JTable(m); table.setRowHeight(28); setPage("User Accounts", new JScrollPane(table));
    }

    private GridBagConstraints gbc(){ GridBagConstraints c=new GridBagConstraints(); c.insets=new Insets(8,8,8,8); c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1; return c; }
    private int field(JPanel p, GridBagConstraints c, int y, String l1, JComponent f1, String l2, JComponent f2){ c.gridwidth=1;c.gridx=0;c.gridy=y;p.add(Theme.label(l1),c);c.gridx=1;p.add(f1,c);c.gridx=2;p.add(Theme.label(l2),c);c.gridx=3;p.add(f2,c);return y+1; }
    private int areaField(JPanel p, GridBagConstraints c, int y, String l, JTextArea a){ c.gridx=0;c.gridy=y;c.gridwidth=1;p.add(Theme.label(l),c);c.gridx=1;c.gridwidth=3;c.fill=GridBagConstraints.BOTH;p.add(new JScrollPane(a),c);c.fill=GridBagConstraints.HORIZONTAL;return y+1; }
    private JTextArea area(){ JTextArea a=new JTextArea(4,20); a.setFont(Theme.bodyFont()); a.setLineWrap(true); a.setWrapStyleWord(true); return a; }
}
