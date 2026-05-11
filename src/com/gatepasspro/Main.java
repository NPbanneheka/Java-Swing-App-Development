package com.gatepasspro;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("Button.arc", 18);
            UIManager.put("Component.arc", 16);
            UIManager.put("TextComponent.arc", 12);
            AppStore store = new AppStore();
            store.load();
            new LoginFrame(store).setVisible(true);
        });
    }
}
