package com.umg.ecotrack;

import javax.swing.SwingUtilities;
import com.umg.ecotrack.ui.LoginForm;

public class EcoTrack {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
