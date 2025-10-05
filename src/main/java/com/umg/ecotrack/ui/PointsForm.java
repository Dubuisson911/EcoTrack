package com.umg.ecotrack.ui;

import com.umg.ecotrack.dao.PointDao;
import com.umg.ecotrack.dao.impl.PointDaoImpl;
import com.umg.ecotrack.model.Point;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PointsForm extends JDialog {
    private final PointDao dao = new PointDaoImpl();

    private final JTextField txtBuscar = new JTextField();
    private final JTable table = new JTable(new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Dirección", "Lat", "Lon", "Responsable"}, 0
    ));

    public PointsForm(Frame owner) {
        super(owner, "Puntos de Reciclaje", true);
        setLayout(new BorderLayout());

        // ----- Top: buscar -----
        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        north.add(new JLabel("Buscar por nombre:"), BorderLayout.WEST);
        north.add(txtBuscar, BorderLayout.CENTER);
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> load());
        north.add(btnRefrescar, BorderLayout.EAST);

        // ----- Centro: tabla -----
        JScrollPane sp = new JScrollPane(table);

        // ----- Bottom: acciones -----
        JPanel south = new JPanel();
        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");

        btnAgregar.addActionListener(e -> openEditor(null));
        btnEditar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { msg("Selecciona una fila"); return; }
            int id = (int) table.getValueAt(row, 0);
            // Cargar desde DAO para editar con datos frescos
            Point p = dao.findById(id);
            if (p == null) { msg("No se pudo cargar el punto seleccionado."); return; }
            openEditor(p);
        });
        btnEliminar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { msg("Selecciona una fila"); return; }
            int id = (int) table.getValueAt(row, 0);
            int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar el punto seleccionado?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                boolean ok = dao.delete(id);
                if (ok) load(); else msg("No se pudo eliminar.");
            }
        });

        south.add(btnAgregar);
        south.add(btnEditar);
        south.add(btnEliminar);

        add(north, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        setSize(900, 450);
        setLocationRelativeTo(owner);
        load();
    }

    private void load() {
        List<Point> data = dao.list(txtBuscar.getText().trim());
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        for (Point p : data) {
            m.addRow(new Object[]{
                    p.getId(), p.getName(), p.getAddress(),
                    p.getLatitude(), p.getLongitude(), p.getManager()
            });
        }
    }

    private void openEditor(Point p) {
        JTextField txtNombre = new JTextField(p == null ? "" : p.getName());
        JTextField txtDireccion = new JTextField(p == null ? "" : p.getAddress());
        JTextField txtLat = new JTextField(p == null || p.getLatitude() == null ? "" : String.valueOf(p.getLatitude()));
        JTextField txtLon = new JTextField(p == null || p.getLongitude() == null ? "" : String.valueOf(p.getLongitude()));
        JTextField txtResp = new JTextField(p == null ? "" : p.getManager());

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Nombre:")); form.add(txtNombre);
        form.add(new JLabel("Dirección:")); form.add(txtDireccion);
        form.add(new JLabel("Latitud:")); form.add(txtLat);
        form.add(new JLabel("Longitud:")); form.add(txtLon);
        form.add(new JLabel("Responsable:")); form.add(txtResp);

        int ok = JOptionPane.showConfirmDialog(this, form,
                p == null ? "Nuevo punto" : "Editar punto",
                JOptionPane.OK_CANCEL_OPTION);

        if (ok == JOptionPane.OK_OPTION) {
            // Validaciones básicas
            String nombre = txtNombre.getText().trim();
            String direccion = txtDireccion.getText().trim();
            if (nombre.isEmpty() || direccion.isEmpty()) {
                msg("Nombre y Dirección son obligatorios.");
                return;
            }

            Double lat = null, lon = null;
            try { if (!txtLat.getText().trim().isEmpty()) lat = Double.parseDouble(txtLat.getText().trim()); }
            catch (Exception ex) { msg("Latitud inválida."); return; }
            try { if (!txtLon.getText().trim().isEmpty()) lon = Double.parseDouble(txtLon.getText().trim()); }
            catch (Exception ex) { msg("Longitud inválida."); return; }

            Point x = (p == null) ? new Point() : p;
            x.setName(nombre);
            x.setAddress(direccion);
            x.setLatitude(lat);
            x.setLongitude(lon);
            x.setManager(txtResp.getText().trim());

            boolean saved = (p == null) ? dao.insert(x) : dao.update(x);
            if (saved) load(); else msg("No se pudo guardar.");
        }
    }

    private void msg(String s) { JOptionPane.showMessageDialog(this, s); }
}
