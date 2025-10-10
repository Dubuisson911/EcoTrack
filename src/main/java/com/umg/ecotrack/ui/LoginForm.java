package com.umg.ecotrack.ui;

import com.umg.ecotrack.dao.UserDao;
import com.umg.ecotrack.dao.impl.UserDaoImpl;
import com.umg.ecotrack.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginForm extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnIngresar, btnSalir;

    public LoginForm() {
        setTitle("EcoTrack — Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 220);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Inicio de sesión", SwingConstants.CENTER);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        content.add(lblTitle, c);

        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 1;
        content.add(new JLabel("Usuario:"), c);

        txtUser = new JTextField();
        c.gridx = 1; c.gridy = 1;
        content.add(txtUser, c);

        c.gridx = 0; c.gridy = 2;
        content.add(new JLabel("Contraseña:"), c);

        txtPass = new JPasswordField();
        c.gridx = 1; c.gridy = 2;
        content.add(txtPass, c);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnSalir = new JButton("Salir");
        btnIngresar = new JButton("Ingresar");
        actions.add(btnSalir);
        actions.add(btnIngresar);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        content.add(actions, c);

        setContentPane(content);

        // Eventos
        btnIngresar.addActionListener(e -> doLogin());
        btnSalir.addActionListener(e -> System.exit(0));
        txtPass.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
    }

    private void doLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese usuario y contraseña.", "EcoTrack", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserDao userDao = new UserDaoImpl();
        User u = userDao.authenticate(user, pass);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Credenciales inválidas.", "EcoTrack", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Abrir menú con el usuario autenticado
        SwingUtilities.invokeLater(() -> new MainMenuForm(u).setVisible(true));
        dispose();
    }
}
