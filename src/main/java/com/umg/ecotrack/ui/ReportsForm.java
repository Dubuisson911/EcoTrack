package com.umg.ecotrack.ui;

import com.umg.ecotrack.dao.CollectionDao;
import com.umg.ecotrack.dao.PointDao;
import com.umg.ecotrack.dao.impl.CollectionDaoImpl;
import com.umg.ecotrack.dao.impl.PointDaoImpl;
import com.umg.ecotrack.export.ExportService;
import com.umg.ecotrack.model.Point;
import com.umg.ecotrack.model.ReportRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsForm extends JDialog {

    private final CollectionDao cdao = new CollectionDaoImpl();
    private final PointDao pdao = new PointDaoImpl();

    private final JTextField txtDesde = new JTextField(10);
    private final JTextField txtHasta = new JTextField(10);
    private final JComboBox<Point> cbPunto = new JComboBox<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mes","Tipo","Total (kg)"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_FILE = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    public ReportsForm(Frame owner) {
        super(owner, "Reporte mensual por tipo", true);
        setSize(820, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        cargarPuntos();
        setMesActual();
        cargar();
    }

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
        tb.add(cbPunto);
        tb.addSeparator();

        JButton btnMes = new JButton("Mes actual");
        btnMes.addActionListener(e -> { setMesActual(); cargar(); });
        JButton btnGenerar = new JButton("Generar");
        btnGenerar.addActionListener(e -> cargar());

        tb.add(btnMes);
        tb.add(btnGenerar);
        return tb;
    }

    private JPanel buildActions() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnExcel = new JButton("Exportar Excel");
        btnExcel.addActionListener(e -> exportarExcel());

        JButton btnPdf = new JButton("Exportar PDF");
        btnPdf.addActionListener(e -> exportarPdf());

        south.add(btnExcel);
        south.add(btnPdf);
        return south;
    }

    private void cargarPuntos() {
        cbPunto.removeAllItems();
        cbPunto.addItem(null); // (Todos)
        for (Point p : pdao.list("")) cbPunto.addItem(p);
        cbPunto.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new DefaultListCellRenderer().getListCellRendererComponent(
                        list, value == null ? "(Todos)" : value.getName(),
                        index, isSelected, cellHasFocus));
    }

    private void setMesActual() {
        LocalDate now = LocalDate.now();
        txtDesde.setText(now.withDayOfMonth(1).format(FMT));
        txtHasta.setText(now.format(FMT));
    }

    private Integer puntoIdSel() {
        Point p = (Point) cbPunto.getSelectedItem();
        return p == null ? null : p.getId();
    }

    private String puntoNombre() {
        Point p = (Point) cbPunto.getSelectedItem();
        return p == null ? "(Todos)" : p.getName();
    }

    private void cargar() {
        LocalDate d1, d2;
        try {
            d1 = LocalDate.parse(txtDesde.getText().trim(), FMT);
            d2 = LocalDate.parse(txtHasta.getText().trim(), FMT);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fechas inválidas. Usa formato yyyy-MM-dd");
            return;
        }

        List<ReportRow> rows = cdao.monthlySumByType(d1, d2, puntoIdSel());
        model.setRowCount(0);
        double total = 0.0;
        for (ReportRow r : rows) {
            model.addRow(new Object[]{r.mes(), r.tipo(), String.format("%.2f", r.totalKg())});
            total += r.totalKg();
        }
        // Fila total
        model.addRow(new Object[]{"", "TOTAL", String.format("%.2f", total)});
    }

    // ====== Exportaciones ======

    private void exportarExcel() {
        List<ReportRow> data = getRowsActuales();
        if (data.isEmpty()) { JOptionPane.showMessageDialog(this, "No hay datos para exportar."); return; }

        String fname = "reporte_" + txtDesde.getText() + "_a_" + txtHasta.getText()
                + "_" + sanitize(puntoNombre()) + ".xlsx";
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File(fname));
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = ensureExt(ch.getSelectedFile(), ".xlsx");

        try {
            ExportService.toXlsx(data, f.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Excel exportado:\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al exportar Excel: " + ex.getMessage());
        }
    }

    private void exportarPdf() {
        List<ReportRow> data = getRowsActuales();
        if (data.isEmpty()) { JOptionPane.showMessageDialog(this, "No hay datos para exportar."); return; }

        String fname = "reporte_" + txtDesde.getText() + "_a_" + txtHasta.getText()
                + "_" + sanitize(puntoNombre()) + ".pdf";
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File(fname));
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = ensureExt(ch.getSelectedFile(), ".pdf");

        String title = "EcoTrack - Reporte mensual por tipo (" + txtDesde.getText() + " a " + txtHasta.getText() + ")";
        String footerLeft = "Generado: " + LocalDateTime.now().format(FMT_FILE);
        String footerRight = "Punto: " + puntoNombre();

        try {
            ExportService.toPdf(data, title, footerLeft, footerRight, f.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "PDF exportado:\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al exportar PDF: " + ex.getMessage());
        }
    }

    // ====== Utilidades ======

    private List<ReportRow> getRowsActuales() {
        // Reconstruye desde la tabla, omitiendo la última fila TOTAL
        int n = model.getRowCount();
        java.util.ArrayList<ReportRow> out = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            String tipo = String.valueOf(model.getValueAt(i,1));
            if ("TOTAL".equals(tipo)) break;
            String mes = String.valueOf(model.getValueAt(i,0));
            double tot = Double.parseDouble(String.valueOf(model.getValueAt(i,2)));
            out.add(new ReportRow(mes, tipo, tot));
        }
        return out;
    }

    private static File ensureExt(File f, String ext) {
        String name = f.getName().toLowerCase();
        if (!name.endsWith(ext)) {
            return new File(f.getAbsolutePath() + ext);
        }
        return f;
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9-_ ]", "").replace(' ', '_');
    }
}
