package com.umg.ecotrack.ui;

import com.umg.ecotrack.dao.CollectionDao;
import com.umg.ecotrack.dao.PointDao;
import com.umg.ecotrack.dao.impl.CollectionDaoImpl;
import com.umg.ecotrack.dao.impl.PointDaoImpl;
import com.umg.ecotrack.model.Collection;
import com.umg.ecotrack.model.Point;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CollectionsForm extends JDialog {

    // DAOs
    private final CollectionDao cdao = new CollectionDaoImpl();
    private final PointDao pdao = new PointDaoImpl();

    // ---- Filtros (en toolbar) ----
    private final JTextField txtDesde = new JTextField(10);
    private final JTextField txtHasta = new JTextField(10);
    private final JComboBox<Point> cbFiltroPunto = new JComboBox<>();
    private final JLabel lblTotal = new JLabel("Total: 0.00 kg");

    // ---- Tabla ----
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","Fecha","Punto","Tipo","Peso (kg)"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    // ---- Panel de edición (derecha) ----
    private final JComboBox<Point> cbPunto = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(10);
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Plástico","Vidrio","Papel"});
    private final JTextField txtPeso = new JTextField(10);
    private final JLabel lblId = new JLabel("(nuevo)");

    private Integer editingId = null; // null = nuevo

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CollectionsForm(Frame owner) {
        super(owner, "Recolecciones", true);
        setSize(1000, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        cargarPuntos();
        setRangoMesActual();
        load();
    }

    // ---------- Construcción UI ----------
    private JToolBar buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tb.add(new JLabel("Desde (yyyy-MM-dd): "));
        tb.add(txtDesde);
        tb.addSeparator();

        tb.add(new JLabel("Hasta: "));
        tb.add(txtHasta);
        tb.addSeparator();

        tb.add(new JLabel("Punto: "));
        tb.add(cbFiltroPunto);
        tb.addSeparator();

        JButton btnMesActual = new JButton("Mes actual");
        btnMesActual.addActionListener(e -> { setRangoMesActual(); load(); });
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> load());

        tb.add(btnMesActual);
        tb.add(btnBuscar);
        tb.add(Box.createHorizontalStrut(20));
        tb.add(lblTotal);

        return tb;
    }

    private JSplitPane buildContent() {
        // Izquierda: tabla
        JScrollPane left = new JScrollPane(table);

        // Derecha: editor fijo
        JPanel right = new JPanel(new GridBagLayout());
        right.setBorder(BorderFactory.createTitledBorder("Editor"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,8,6,8);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.LINE_END;
        right.add(new JLabel("ID:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        right.add(lblId, c);

        c.gridy++; c.gridx = 0; c.anchor = GridBagConstraints.LINE_END;
        right.add(new JLabel("Punto:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        right.add(cbPunto, c);

        c.gridy++; c.gridx = 0; c.anchor = GridBagConstraints.LINE_END;
        right.add(new JLabel("Fecha (yyyy-MM-dd):"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        right.add(txtFecha, c);

        c.gridy++; c.gridx = 0; c.anchor = GridBagConstraints.LINE_END;
        right.add(new JLabel("Tipo:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        right.add(cbTipo, c);

        c.gridy++; c.gridx = 0; c.anchor = GridBagConstraints.LINE_END;
        right.add(new JLabel("Peso (kg):"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        right.add(txtPeso, c);

        // Sin botones aquí; van abajo para diferenciar aún más del PointsForm

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(620);
        return split;
    }

    private JPanel buildActions() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo   = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar= new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar filtros");

        btnNuevo.addActionListener(e -> limpiarEditor());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> { setRangoMesActual(); cbFiltroPunto.setSelectedItem(null); load(); });

        south.add(btnNuevo);
        south.add(btnGuardar);
        south.add(btnEliminar);
        south.add(btnLimpiar);
        return south;
    }

    // ---------- Lógica ----------
    private void cargarPuntos() {
        cbFiltroPunto.removeAllItems();
        cbFiltroPunto.addItem(null); // (Todos)

        cbPunto.removeAllItems();

        List<Point> puntos = pdao.list("");
        for (Point p : puntos) {
            cbFiltroPunto.addItem(p);
            cbPunto.addItem(p);
        }

        // Renderers para mostrar nombre
        cbFiltroPunto.setRenderer((list, value, index, isSelected, cellHasFocus) ->
            new DefaultListCellRenderer().getListCellRendererComponent(
                list, value == null ? "(Todos)" : value.getName(), index, isSelected, cellHasFocus));
        cbPunto.setRenderer((list, value, index, isSelected, cellHasFocus) ->
            new DefaultListCellRenderer().getListCellRendererComponent(
                list, value == null ? "" : value.getName(), index, isSelected, cellHasFocus));
    }

    private void setRangoMesActual() {
        LocalDate now = LocalDate.now();
        txtDesde.setText(now.withDayOfMonth(1).format(FMT));
        txtHasta.setText(now.format(FMT));
        txtFecha.setText(now.format(FMT)); // default para editor
    }

    private LocalDate parseDate(String s, String nombreCampo) {
        try {
            return LocalDate.parse(s.trim(), FMT);
        } catch (DateTimeParseException ex) {
            msg(nombreCampo + " inválida. Usa formato yyyy-MM-dd");
            return null;
        }
    }

    private Integer filtroPointId() {
        Point p = (Point) cbFiltroPunto.getSelectedItem();
        return p == null ? null : p.getId();
    }

    private void load() {
        LocalDate d1 = parseDate(txtDesde.getText(), "Desde");
        LocalDate d2 = parseDate(txtHasta.getText(), "Hasta");
        if (d1 == null || d2 == null) return;

        var rows = cdao.list(d1, d2, filtroPointId());
        model.setRowCount(0);

        double total = 0.0;
        for (Collection c : rows) {
            Point p = pdao.findById(c.getPointId());
            model.addRow(new Object[]{
                    c.getId(),
                    c.getDate().format(FMT),
                    p != null ? p.getName() : ("#" + c.getPointId()),
                    c.getType(),
                    String.format("%.2f", c.getWeightKg())
            });
            total += c.getWeightKg();
        }
        lblTotal.setText("Total: " + String.format("%.2f", total) + " kg");

        // seleccionar la primera fila para editar rápido
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0,0);
            cargarEditorDesdeTabla();
        } else {
            limpiarEditor();
        }

        // Al hacer click en la tabla, cargar al editor
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarEditorDesdeTabla();
        });
    }

    private void cargarEditorDesdeTabla() {
        int row = table.getSelectedRow();
        if (row < 0) { limpiarEditor(); return; }

        this.editingId = (Integer) table.getValueAt(row, 0);
        lblId.setText(String.valueOf(editingId));
        txtFecha.setText((String) table.getValueAt(row, 1));

        // Punto: buscar por nombre
        String name = (String) table.getValueAt(row, 2);
        for (int i=0; i<cbPunto.getItemCount(); i++) {
            Point p = cbPunto.getItemAt(i);
            if (p.getName().equals(name)) { cbPunto.setSelectedIndex(i); break; }
        }

        cbTipo.setSelectedItem((String) table.getValueAt(row, 3));
        txtPeso.setText(((String) table.getValueAt(row, 4)).replace(",", "."));
    }

    private void limpiarEditor() {
        this.editingId = null;
        lblId.setText("(nuevo)");
        if (cbPunto.getItemCount() > 0) cbPunto.setSelectedIndex(0);
        txtFecha.setText(LocalDate.now().format(FMT));
        cbTipo.setSelectedIndex(0);
        txtPeso.setText("");
    }

    private void guardar() {
        // Validaciones editor
        Point p = (Point) cbPunto.getSelectedItem();
        if (p == null) { msg("Seleccione un punto."); return; }
        LocalDate fecha = parseDate(txtFecha.getText(), "Fecha");
        if (fecha == null) return;

        String tipo = (String) cbTipo.getSelectedItem();
        if (tipo == null || tipo.isBlank()) { msg("Seleccione un tipo."); return; }

        double peso;
        try {
            peso = Double.parseDouble(txtPeso.getText().trim());
            if (peso <= 0) { msg("El peso debe ser mayor a 0."); return; }
        } catch (Exception ex) { msg("Peso inválido."); return; }

        Collection c = new Collection();
        if (editingId != null) c.setId(editingId);
        c.setPointId(p.getId());
        c.setDate(fecha);
        c.setType(tipo);
        c.setWeightKg(peso);

        boolean ok = (editingId == null) ? cdao.insert(c) : cdao.update(c);
        if (ok) { load(); msg("Guardado con éxito."); }
        else msg("No se pudo guardar (verifica el mensaje de error).");
    }

    private void eliminar() {
        int row = table.getSelectedRow();
        if (row < 0) { msg("Selecciona una fila."); return; }
        int id = (Integer) table.getValueAt(row, 0);
        int opt = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la recolección #" + id + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            boolean ok = cdao.delete(id);
            if (ok) { load(); msg("Eliminado."); }
            else msg("No se pudo eliminar (verifica el mensaje de error).");
        }
    }

    private void msg(String s) { JOptionPane.showMessageDialog(this, s); }
}
