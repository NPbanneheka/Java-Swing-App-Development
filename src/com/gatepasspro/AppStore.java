package com.gatepasspro;

import java.io.*;
import javax.swing.JOptionPane;
import java.nio.file.*;
import java.util.*;

class AppStore implements Serializable {
    private static final long serialVersionUID = 1L;
    private final File dataFile = new File("data/app-data.ser");
    ArrayList<User> users = new ArrayList<>();
    ArrayList<GatePass> gatePasses = new ArrayList<>();
    ArrayList<ScheduleRecord> schedules = new ArrayList<>();

    @SuppressWarnings("unchecked")
    void load() {
        try {
            Files.createDirectories(Paths.get("data/profile_photos"));
            if (!dataFile.exists()) {
                seed();
                save();
                return;
            }
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(dataFile))) {
                users = (ArrayList<User>) in.readObject();
                gatePasses = (ArrayList<GatePass>) in.readObject();
                schedules = (ArrayList<ScheduleRecord>) in.readObject();
            }
            if (users.stream().noneMatch(u -> u.username.equalsIgnoreCase("admin"))) {
                users.add(new User(UUID.randomUUID().toString(), "System Administrator", "admin", "admin123", "Admin", ""));
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
            seed();
            save();
        }
    }

    void seed() {
        users.clear();
        gatePasses.clear();
        schedules.clear();
        users.add(new User(UUID.randomUUID().toString(), "System Administrator", "admin", "admin123", "Admin", ""));
    }

    void save() {
        try {
            Files.createDirectories(Paths.get("data/profile_photos"));
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                out.writeObject(users);
                out.writeObject(gatePasses);
                out.writeObject(schedules);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Data save error: " + e.getMessage());
        }
    }

    Optional<User> login(String username, String password) {
        return users.stream()
                .filter(u -> u.username.equalsIgnoreCase(username.trim()) && u.password.equals(password) && "Active".equals(u.status))
                .findFirst();
    }

    boolean usernameExists(String username) {
        return users.stream().anyMatch(u -> u.username.equalsIgnoreCase(username.trim()));
    }

    void addUser(User user) {
        users.add(user);
        save();
    }

    String nextGatePassId() {
        return String.format("GP-%05d", gatePasses.size() + 1);
    }

    String nextScheduleId() {
        return String.format("SCH-%05d", schedules.size() + 1);
    }

    Optional<GatePass> findGatePass(String id) {
        return gatePasses.stream().filter(g -> g.id.equalsIgnoreCase(id.trim())).findFirst();
    }
}
