package com.gateflowpro;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class GateFlowPro {
    static final String APP = "GateFlowPro Desktop";
    static Store store = Store.load();
    static User currentUser;

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    static class C {
        static final Color NAVY = new Color(15, 23, 42);
        static final Color NAVY2 = new Color(30, 41, 59);
        static final Color BLUE = new Color(37, 99, 235);
        static final Color BG = new Color(245, 247, 251);
        static final Color CARD = Color.WHITE;
        static final Color BORDER = new Color(226, 232, 240);
        static final Color MUTED = new Color(100, 116, 139);
        static final Font TITLE = new Font("Segoe UI", Font.BOLD, 28);
        static final Font H2 = new Font("Segoe UI", Font.BOLD, 20);
        static final Font H3 = new Font("Segoe UI", Font.BOLD, 15);
        static final Font TEXT = new Font("Segoe UI", Font.PLAIN, 14);
        static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);
    }

    static class UI {
        static JLabel label(String text) { JLabel l = new JLabel(text); l.setFont(C.BOLD); return l; }
        static JLabel muted(String text) { JLabel l = new JLabel(text); l.setFont(C.TEXT); l.setForeground(C.MUTED); return l; }
        static JTextField field() { JTextField f = new JTextField(); styleField(f); return f; }
        static JPasswordField pass() { JPasswordField f = new JPasswordField(); styleField(f); return f; }
        static void styleField(JComponent f) { f.setFont(C.TEXT); f.setBorder(new CompoundBorder(new LineBorder(C.BORDER), new EmptyBorder(10,12,10,12))); f.setBackground(Color.WHITE); }
        static JButton btn(String text) { JButton b = new JButton(text); b.setFont(C.BOLD); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(12,18,12,18)); b.setBackground(C.BLUE); b.setForeground(Color.WHITE); return b; }
        static JButton ghost(String text) { JButton b = btn(text); b.setBackground(new Color(226,232,240)); b.setForeground(C.NAVY); return b; }
        static JPanel card() { JPanel p = new JPanel(); p.setBackground(C.CARD); p.setBorder(new CompoundBorder(new LineBorder(C.BORDER), new EmptyBorder(22,24,22,24))); return p; }
        static GridBagConstraints gbc(int x, int y) { GridBagConstraints g = new GridBagConstraints(); g.gridx=x; g.gridy=y; g.insets=new Insets(7,7,7,7); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1; return g; }
        static void msg(Component c, String m) { JOptionPane.showMessageDialog(c, m, APP, JOptionPane.INFORMATION_MESSAGE); }
        static void err(Component c, String m) { JOptionPane.showMessageDialog(c, m, APP, JOptionPane.ERROR_MESSAGE); }
    }

    static class LoginFrame extends JFrame {
        JTextField username = UI.field(); JPasswordField password = UI.pass();
        LoginFrame() {
            setTitle(APP + " - Login"); setSize(1100, 680); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
            JPanel root = new JPanel(new GridLayout(1,2)); setContentPane(root);
            JPanel hero = new JPanel(new GridBagLayout()); hero.setBackground(C.NAVY); hero.setBorder(new EmptyBorder(45,55,45,55));
            JPanel h = new JPanel(); h.setOpaque(false); h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
            JLabel logo = new JLabel("GateFlowPro"); logo.setForeground(Color.WHITE); logo.setFont(new Font("Segoe UI", Font.BOLD, 42));
            JLabel sub = new JLabel("Official Gate Pass & Schedule Management"); sub.setForeground(new Color(203,213,225)); sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            JLabel info = new JLabel("Local desktop application • Privacy first • No internet required"); info.setForeground(new Color(148,163,184)); info.setFont(C.TEXT);
            h.add(logo); h.add(Box.createVerticalStrut(12)); h.add(sub); h.add(Box.createVerticalStrut(28)); h.add(info); hero.add(h);
            root.add(hero);
            JPanel right = new JPanel(new GridBagLayout()); right.setBackground(C.BG); root.add(right);
            JPanel card = UI.card(); card.setPreferredSize(new Dimension(430, 430)); card.setLayout(new GridBagLayout());
            JLabel title = new JLabel("Welcome Back"); title.setFont(C.TITLE); card.add(title, UI.gbc(0,0));
            card.add(UI.muted("Sign in to continue to your secure desktop workspace"), UI.gbc(0,1));
            card.add(UI.label("Username"), UI.gbc(0,2)); card.add(username, UI.gbc(0,3));
            card.add(UI.label("Password"), UI.gbc(0,4)); card.add(password, UI.gbc(0,5));
            JButton login = UI.btn("Login"); card.add(login, UI.gbc(0,6));
            JButton create = UI.ghost("Create New Account"); card.add(create, UI.gbc(0,7));
            JLabel def = UI.muted("Default admin: admin / admin123"); card.add(def, UI.gbc(0,8));
            right.add(card);
            login.addActionListener(e -> doLogin());
            create.addActionListener(e -> new RegisterDialog(this).setVisible(true));
            getRootPane().setDefaultButton(login);
        }
        void doLogin(){
            String u=username.getText().trim(); String p=new String(password.getPassword());
            User user = store.findUser(u);
            if(user==null || !user.password.equals(p)){ UI.err(this,"Invalid username or password."); return; }
            currentUser=user; dispose(); new MainFrame().setVisible(true);
        }
    }

    static class RegisterDialog extends JDialog {
        JTextField full=UI.field(), user=UI.field(), phone=UI.field(); JPasswordField pass=UI.pass(); JComboBox<String> role=new JComboBox<>(new String[]{"Gate Officer","Schedule Officer","Supervisor"});
        RegisterDialog(JFrame parent){ super(parent,"Create Account",true); setSize(470,520); setLocationRelativeTo(parent); setLayout(new BorderLayout());
            JPanel p=UI.card(); p.setLayout(new GridBagLayout()); add(p);
            JLabel t=new JLabel("Create User Account"); t.setFont(C.H2); p.add(t, UI.gbc(0,0));
            p.add(UI.label("Full Name"),UI.gbc(0,1)); p.add(full,UI.gbc(0,2));
            p.add(UI.label("Username"),UI.gbc(0,3)); p.add(user,UI.gbc(0,4));
            p.add(UI.label("Password"),UI.gbc(0,5)); p.add(pass,UI.gbc(0,6));
            p.add(UI.label("Phone"),UI.gbc(0,7)); p.add(phone,UI.gbc(0,8));
            p.add(UI.label("Role"),UI.gbc(0,9)); role.setFont(C.TEXT); p.add(role,UI.gbc(0,10));
            JButton save=UI.btn("Create Account"); p.add(save,UI.gbc(0,11));
            save.addActionListener(e->save());
        }
        void save(){
            if(full.getText().trim().isEmpty()||user.getText().trim().isEmpty()||pass.getPassword().length<4){ UI.err(this,"Please enter full name, username and password with at least 4 characters."); return; }
            if(store.findUser(user.getText().trim())!=null){ UI.err(this,"Username already exists."); return; }
            User u=new User(); u.fullName=full.getText().trim(); u.username=user.getText().trim(); u.password=new String(pass.getPassword()); u.phone=phone.getText().trim(); u.role=role.getSelectedItem().toString();
            store.users.add(u); store.save(); UI.msg(this,"Account created successfully."); dispose();
        }
    }

    static class MainFrame extends JFrame {
        CardLayout cards = new CardLayout(); JPanel content = new JPanel(cards); JLabel title = new JLabel("Dashboard"); JLabel topUser = new JLabel(); PhotoLabel topPhoto = new PhotoLabel(44,44);
        DashboardPanel dash; IssuePanel issue; VerifyPanel verify; SearchPanel search; ReportsPanel reports; ProfilePanel profile; UsersPanel users;
        MainFrame(){
            setTitle(APP + " - Dashboard"); setSize(1240,760); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
            JPanel root=new JPanel(new BorderLayout()); setContentPane(root);
            root.add(topbar(),BorderLayout.NORTH); root.add(sidebar(),BorderLayout.WEST);
            content.setBackground(C.BG); root.add(content,BorderLayout.CENTER);
            dash=new DashboardPanel(); issue=new IssuePanel(); verify=new VerifyPanel(); search=new SearchPanel(); reports=new ReportsPanel(); profile=new ProfilePanel(); users=new UsersPanel();
            addPage("Dashboard",dash); addPage("Issue Gate Pass",issue); addPage("Verify & Schedule",verify); addPage("Search Records",search); addPage("Reports",reports); addPage("My Profile",profile); addPage("Users",users);
            refreshHeader();
        }
        JPanel topbar(){ JPanel p=new JPanel(new BorderLayout()); p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(14,22,14,22)); title.setFont(C.H2); p.add(title,BorderLayout.WEST); JPanel r=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0)); r.setOpaque(false); topUser.setFont(C.TEXT); r.add(topUser); r.add(topPhoto); p.add(r,BorderLayout.EAST); return p; }
        JPanel sidebar(){ JPanel s=new JPanel(); s.setLayout(new BoxLayout(s,BoxLayout.Y_AXIS)); s.setPreferredSize(new Dimension(245,0)); s.setBackground(C.NAVY); s.setBorder(new EmptyBorder(26,18,26,18)); JLabel brand=new JLabel("GateFlowPro"); brand.setForeground(Color.WHITE); brand.setFont(new Font("Segoe UI",Font.BOLD,26)); s.add(brand); JLabel role=new JLabel(currentUser.role); role.setForeground(new Color(203,213,225)); role.setFont(C.TEXT); s.add(role); s.add(Box.createVerticalStrut(25));
            String[] pages={"Dashboard","Issue Gate Pass","Verify & Schedule","Search Records","Reports","My Profile","Users"}; for(String pg:pages) s.add(nav(pg)); s.add(Box.createVerticalGlue()); JButton lo=nav("Logout"); lo.addActionListener(e->{dispose(); new LoginFrame().setVisible(true);}); s.add(lo); return s; }
        JButton nav(String page){ JButton b=new JButton(page); b.setMaximumSize(new Dimension(220,44)); b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setFocusPainted(false); b.setHorizontalAlignment(SwingConstants.LEFT); b.setBorder(new EmptyBorder(12,14,12,14)); b.setBackground(C.NAVY2); b.setForeground(Color.WHITE); b.setFont(C.BOLD); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); if(!page.equals("Logout")) b.addActionListener(e->show(page)); return b; }
        void addPage(String name,JPanel p){ content.add(wrap(p),name); }
        JPanel wrap(JPanel inner){ JPanel w=new JPanel(new BorderLayout()); w.setBackground(C.BG); w.setBorder(new EmptyBorder(24,24,24,24)); w.add(inner,BorderLayout.CENTER); return w; }
        void show(String page){ title.setText(page); cards.show(content,page); refreshAll(); }
        void refreshHeader(){ topUser.setText(currentUser.fullName); topPhoto.setImage(currentUser.photoPath); }
        void refreshAll(){ dash.refresh(); verify.refresh(); search.refresh(); reports.refresh(); profile.load(); users.refresh(); refreshHeader(); }
    }

    static class DashboardPanel extends JPanel { JLabel gp=new JLabel(), ver=new JLabel(), sch=new JLabel(), us=new JLabel(); DashboardPanel(){ setOpaque(false); setLayout(new BorderLayout(0,20)); JPanel grid=new JPanel(new GridLayout(1,4,18,18)); grid.setOpaque(false); add(grid,BorderLayout.NORTH); grid.add(metric("Total Gate Passes",gp)); grid.add(metric("Verified Returns",ver)); grid.add(metric("Schedules Issued",sch)); grid.add(metric("System Users",us)); JTextArea area=new JTextArea("Today Workflow\n\n1. Issue gate pass before vehicle leaves.\n2. Driver returns with invoice and seal confirmation.\n3. Verify returned documents and goods status.\n4. Issue schedule after verification.\n5. Search or print report when required."); area.setFont(new Font("Segoe UI",Font.PLAIN,16)); area.setEditable(false); area.setBorder(new EmptyBorder(25,25,25,25)); add(area,BorderLayout.CENTER); refresh(); }
        JPanel metric(String name,JLabel value){ JPanel c=UI.card(); c.setLayout(new BorderLayout()); JLabel n=UI.muted(name); value.setFont(new Font("Segoe UI",Font.BOLD,34)); c.add(n,BorderLayout.NORTH); c.add(value,BorderLayout.CENTER); return c; } void refresh(){ gp.setText(""+store.passes.size()); ver.setText(""+store.passes.stream().filter(x->x.verified).count()); sch.setText(""+store.passes.stream().filter(x->x.scheduleIssued).count()); us.setText(""+store.users.size()); }}

    static class IssuePanel extends JPanel { JTextField vehicle=UI.field(), driver=UI.field(), phone=UI.field(), invoice=UI.field(), route=UI.field(), goods=UI.field(); JTextArea remarks=new JTextArea(4,20);
        IssuePanel(){ setOpaque(false); setLayout(new BorderLayout()); JPanel c=UI.card(); c.setLayout(new GridBagLayout()); add(c,BorderLayout.NORTH); remarks.setFont(C.TEXT); remarks.setBorder(new CompoundBorder(new LineBorder(C.BORDER),new EmptyBorder(8,8,8,8)));
            addRow(c,0,"Vehicle Number",vehicle,"Driver Name",driver); addRow(c,2,"Phone",phone,"Invoice Number",invoice); addRow(c,4,"Route / Customer",route,"Goods / Load Details",goods); GridBagConstraints g=UI.gbc(0,6); g.gridwidth=2; c.add(UI.label("Remarks"),g); g=UI.gbc(0,7); g.gridwidth=2; c.add(new JScrollPane(remarks),g); JButton save=UI.btn("Issue Gate Pass"); g=UI.gbc(0,8); g.gridwidth=2; c.add(save,g); save.addActionListener(e->save()); }
        void addRow(JPanel p,int y,String a,JComponent af,String b,JComponent bf){ p.add(UI.label(a),UI.gbc(0,y)); p.add(UI.label(b),UI.gbc(1,y)); p.add(af,UI.gbc(0,y+1)); p.add(bf,UI.gbc(1,y+1)); }
        void save(){ if(vehicle.getText().trim().isEmpty()||driver.getText().trim().isEmpty()){ UI.err(this,"Vehicle number and driver name are required."); return; } GatePass gp=new GatePass(); gp.id="GP"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); gp.vehicle=vehicle.getText().trim(); gp.driver=driver.getText().trim(); gp.phone=phone.getText().trim(); gp.invoice=invoice.getText().trim(); gp.route=route.getText().trim(); gp.goods=goods.getText().trim(); gp.remarks=remarks.getText().trim(); gp.issuedBy=currentUser.username; gp.issuedAt=new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()); store.passes.add(gp); store.save(); clear(); UI.msg(this,"Gate pass issued successfully. ID: "+gp.id); }
        void clear(){ for(JTextField f:new JTextField[]{vehicle,driver,phone,invoice,route,goods}) f.setText(""); remarks.setText(""); }}

    static class VerifyPanel extends JPanel { DefaultTableModel model; JTable table; JTextField seal=UI.field(), schedule=UI.field(); JTextArea notes=new JTextArea(4,20); VerifyPanel(){ setOpaque(false); setLayout(new BorderLayout(0,15)); model=new DefaultTableModel(new String[]{"ID","Vehicle","Driver","Invoice","Verified","Schedule"},0){ public boolean isCellEditable(int r,int c){return false;}}; table=new JTable(model); table.setRowHeight(30); add(new JScrollPane(table),BorderLayout.CENTER); JPanel c=UI.card(); c.setLayout(new GridBagLayout()); add(c,BorderLayout.SOUTH); notes.setBorder(new CompoundBorder(new LineBorder(C.BORDER),new EmptyBorder(8,8,8,8))); c.add(UI.label("Seal / Invoice Check Details"),UI.gbc(0,0)); c.add(UI.label("Schedule Number"),UI.gbc(1,0)); c.add(seal,UI.gbc(0,1)); c.add(schedule,UI.gbc(1,1)); GridBagConstraints g=UI.gbc(0,2); g.gridwidth=2; c.add(UI.label("Verification Notes"),g); g=UI.gbc(0,3); g.gridwidth=2; c.add(new JScrollPane(notes),g); JButton btn=UI.btn("Verify Return & Issue Schedule"); g=UI.gbc(0,4); g.gridwidth=2; c.add(btn,g); btn.addActionListener(e->verify()); refresh(); }
        void refresh(){ model.setRowCount(0); for(GatePass p:store.passes) model.addRow(new Object[]{p.id,p.vehicle,p.driver,p.invoice,p.verified?"Yes":"No",p.scheduleIssued?p.scheduleNo:"Pending"}); }
        void verify(){ int r=table.getSelectedRow(); if(r<0){UI.err(this,"Select a gate pass record first.");return;} String id=model.getValueAt(r,0).toString(); GatePass p=store.findPass(id); if(p==null)return; p.verified=true; p.scheduleIssued=true; p.sealCheck=seal.getText().trim(); p.scheduleNo=schedule.getText().trim().isEmpty()?"SCH-"+p.id.substring(2):schedule.getText().trim(); p.verifyNotes=notes.getText().trim(); p.verifiedBy=currentUser.username; p.verifiedAt=new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()); store.save(); refresh(); UI.msg(this,"Verification completed and schedule issued."); }}

    static class SearchPanel extends JPanel { JTextField q=UI.field(); DefaultTableModel model; SearchPanel(){ setOpaque(false); setLayout(new BorderLayout(0,15)); JPanel top=UI.card(); top.setLayout(new BorderLayout(12,0)); top.add(q,BorderLayout.CENTER); JButton b=UI.btn("Search"); top.add(b,BorderLayout.EAST); add(top,BorderLayout.NORTH); model=new DefaultTableModel(new String[]{"ID","Vehicle","Driver","Invoice","Route","Schedule","Date"},0); JTable t=new JTable(model); t.setRowHeight(30); add(new JScrollPane(t),BorderLayout.CENTER); b.addActionListener(e->refresh()); q.addActionListener(e->refresh()); refresh(); } void refresh(){ String s=q.getText().trim().toLowerCase(); model.setRowCount(0); for(GatePass p:store.passes){ String all=(p.id+p.vehicle+p.driver+p.invoice+p.route+p.scheduleNo).toLowerCase(); if(s.isEmpty()||all.contains(s)) model.addRow(new Object[]{p.id,p.vehicle,p.driver,p.invoice,p.route,p.scheduleNo,p.issuedAt}); } }}

    static class ReportsPanel extends JPanel { DefaultTableModel model; JTable table; ReportsPanel(){ setOpaque(false); setLayout(new BorderLayout(0,15)); JPanel top=UI.card(); top.setLayout(new FlowLayout(FlowLayout.RIGHT)); JButton ref=UI.ghost("Refresh"); JButton print=UI.btn("Print Report"); top.add(ref); top.add(print); add(top,BorderLayout.NORTH); model=new DefaultTableModel(new String[]{"ID","Vehicle","Driver","Invoice","Issued By","Verified By","Schedule","Status"},0); table=new JTable(model); table.setRowHeight(30); add(new JScrollPane(table),BorderLayout.CENTER); ref.addActionListener(e->refresh()); print.addActionListener(e->{try{table.print();}catch(Exception ex){UI.err(this,ex.getMessage());}}); refresh(); } void refresh(){ model.setRowCount(0); for(GatePass p:store.passes) model.addRow(new Object[]{p.id,p.vehicle,p.driver,p.invoice,p.issuedBy,p.verifiedBy,p.scheduleNo,p.scheduleIssued?"Completed":"Pending"}); }}

    static class ProfilePanel extends JPanel { PhotoLabel photo=new PhotoLabel(150,150); JTextField name=UI.field(), phone=UI.field(); JPasswordField pass=UI.pass(); JComboBox<String> fit=new JComboBox<>(new String[]{"Cover","Contain"}); ProfilePanel(){ setOpaque(false); setLayout(new BorderLayout()); JPanel c=UI.card(); c.setLayout(new GridBagLayout()); add(c,BorderLayout.NORTH); GridBagConstraints g=UI.gbc(0,0); g.gridwidth=2; g.anchor=GridBagConstraints.CENTER; c.add(photo,g); JButton up=UI.ghost("Upload / Change Photo"); g=UI.gbc(0,1); g.gridwidth=2; c.add(up,g); c.add(UI.label("Full Name"),UI.gbc(0,2)); c.add(UI.label("Phone"),UI.gbc(1,2)); c.add(name,UI.gbc(0,3)); c.add(phone,UI.gbc(1,3)); c.add(UI.label("Password"),UI.gbc(0,4)); c.add(UI.label("Photo Fit"),UI.gbc(1,4)); c.add(pass,UI.gbc(0,5)); c.add(fit,UI.gbc(1,5)); JButton save=UI.btn("Save Profile"); g=UI.gbc(0,6); g.gridwidth=2; c.add(save,g); up.addActionListener(e->upload()); save.addActionListener(e->save()); load(); }
        void load(){ if(currentUser==null)return; name.setText(currentUser.fullName); phone.setText(currentUser.phone); pass.setText(currentUser.password); photo.setImage(currentUser.photoPath); fit.setSelectedItem(currentUser.photoFit); }
        void upload(){ JFileChooser fc=new JFileChooser(); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){ try{ Path src=fc.getSelectedFile().toPath(); Files.createDirectories(Paths.get("data/photos")); Path dst=Paths.get("data/photos", currentUser.username+"_"+src.getFileName().toString()); Files.copy(src,dst,StandardCopyOption.REPLACE_EXISTING); currentUser.photoPath=dst.toString(); photo.setImage(currentUser.photoPath); store.save(); }catch(Exception ex){UI.err(this,ex.getMessage());}} }
        void save(){ currentUser.fullName=name.getText().trim(); currentUser.phone=phone.getText().trim(); currentUser.password=new String(pass.getPassword()); currentUser.photoFit=fit.getSelectedItem().toString(); store.save(); load(); UI.msg(this,"Profile saved."); }}

    static class UsersPanel extends JPanel { DefaultTableModel model; UsersPanel(){ setOpaque(false); setLayout(new BorderLayout()); model=new DefaultTableModel(new String[]{"Full Name","Username","Role","Phone"},0); JTable t=new JTable(model); t.setRowHeight(30); add(new JScrollPane(t),BorderLayout.CENTER); refresh(); } void refresh(){ model.setRowCount(0); for(User u:store.users) model.addRow(new Object[]{u.fullName,u.username,u.role,u.phone}); }}

    static class PhotoLabel extends JLabel { int w,h; String path; PhotoLabel(int w,int h){this.w=w;this.h=h;setPreferredSize(new Dimension(w,h));setOpaque(true);setBackground(new Color(226,232,240));setHorizontalAlignment(CENTER);setBorder(new LineBorder(C.BORDER));} void setImage(String p){path=p; repaint();} protected void paintComponent(Graphics gr){ super.paintComponent(gr); Graphics2D g=(Graphics2D)gr; g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); if(path!=null&&!path.isEmpty()&&Files.exists(Paths.get(path))){ try{ ImageIcon ic=new ImageIcon(path); Image img=ic.getImage(); double iw=ic.getIconWidth(), ih=ic.getIconHeight(); double scale="Contain".equals(currentUser==null?"Cover":currentUser.photoFit)?Math.min(w/iw,h/ih):Math.max(w/iw,h/ih); int nw=(int)(iw*scale), nh=(int)(ih*scale); g.drawImage(img,(w-nw)/2,(h-nh)/2,nw,nh,this); return;}catch(Exception e){} } g.setColor(C.MUTED); g.setFont(C.BOLD); g.drawString("PHOTO",w/2-24,h/2+5); }}

    static class Store implements Serializable { List<User> users=new ArrayList<>(); List<GatePass> passes=new ArrayList<>(); static final String FILE="data/gateflowpro.dat"; static Store load(){ try{ Files.createDirectories(Paths.get("data")); if(Files.exists(Paths.get(FILE))){ ObjectInputStream in=new ObjectInputStream(new FileInputStream(FILE)); Store s=(Store)in.readObject(); in.close(); if(s.users.stream().noneMatch(u->u.username.equals("admin"))) s.addAdmin(); return s; }}catch(Exception e){} Store s=new Store(); s.addAdmin(); s.save(); return s; } void addAdmin(){ User a=new User(); a.fullName="System Administrator"; a.username="admin"; a.password="admin123"; a.role="Admin"; users.add(a); } void save(){ try{ Files.createDirectories(Paths.get("data")); ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(FILE)); out.writeObject(this); out.close(); }catch(Exception e){ e.printStackTrace(); }} User findUser(String un){ for(User u:users) if(u.username.equalsIgnoreCase(un)) return u; return null;} GatePass findPass(String id){ for(GatePass p:passes) if(p.id.equals(id)) return p; return null;} }
    static class User implements Serializable { String fullName="", username="", password="", phone="", role="Gate Officer", photoPath="", photoFit="Cover"; }
    static class GatePass implements Serializable { String id="", vehicle="", driver="", phone="", invoice="", route="", goods="", remarks="", issuedBy="", issuedAt="", sealCheck="", scheduleNo="", verifyNotes="", verifiedBy="", verifiedAt=""; boolean verified=false, scheduleIssued=false; }
}
