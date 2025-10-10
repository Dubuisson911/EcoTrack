package com.umg.ecotrack.ui;

import com.umg.ecotrack.model.User;

import javax.swing.*;
import java.awt.*;

public class MainMenuForm extends JFrame {

    private final User currentUser;

    private JButton btnPuntos;
    private JButton btnRecolecciones;
    private JButton btnReportes;
    private JButton btnSalir;

    public MainMenuForm(User u) {
        this.currentUser = u;
        initComponents();
        applyRole();
    }

    private void initComponents() {
        setTitle("EcoTrack - Menú Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 340);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel();
        root.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Menú Principal de EcoTrack", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(0,120,0));

        btnPuntos = new JButton("Puntos de Reciclaje");
        btnRecolecciones = new JButton("Recolecciones");
        btnReportes = new JButton("Reportes");
        btnSalir = new JButton("Salir");

        Dimension d = new Dimension(240, 36);
        for (JButton b : new JButton[]{btnPuntos, btnRecolecciones, btnReportes, btnSalir}) {
            b.setMaximumSize(d);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        root.add(title);
        root.add(Box.createVerticalStrut(18));
        root.add(btnPuntos);
        root.add(Box.createVerticalStrut(12));
        root.add(btnRecolecciones);
        root.add(Box.createVerticalStrut(12));
        root.add(btnReportes);
        root.add(Box.createVerticalStrut(18));
        root.add(btnSalir);

        setContentPane(root);

        // ==== ABRIR TUS FORMULARIOS (constructor Frame) ====
        btnPuntos.addActionListener(e -> {
            // Tu PointsForm tiene ctor(Frame)
            new PointsForm(this).setVisible(true);
        });

        btnRecolecciones.addActionListener(e -> {
            // Tu CollectionsForm tiene ctor(Frame)
            new CollectionsForm(this).setVisible(true);
        });

        btnReportes.addActionListener(e -> {
            // Tu ReportsForm tiene ctor(Frame)
            new ReportsForm(this).setVisible(true);
        });

        btnSalir.addActionListener(e -> System.exit(0));
    }

    private void applyRole() {
        if (currentUser == null) return;
        boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());

        // Ejemplo: solo admin puede usar "Reportes"
        btnReportes.setEnabled(isAdmin);
        // o para ocultar completamente:
        // btnReportes.setVisible(isAdmin);

        setTitle(getTitle() + " — Usuario: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
    }
}
