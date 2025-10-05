package com.umg.ecotrack;

// ---- IMPORTA SOLO UNO SEGÚN LA FASE QUE QUIERAS PROBAR ----
// import com.umg.ecotrack.ui.PointsForm;       // Fase B - Puntos
// import com.umg.ecotrack.ui.CollectionsForm;  // Fase C - Recolecciones
import com.umg.ecotrack.ui.ReportsForm;         // Fase D - Reportes

public class EcoTrack {
    public static void main(String[] args) {
        // Obliga a modo GUI (por seguridad)
        System.setProperty("java.awt.headless", "false");

        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Aplica el estilo del sistema operativo
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // --------- FORMULARIO PRINCIPAL ---------

            // 🔹 Código anterior comentado (no se elimina para conservarlo)
            /*
            PointsForm dlgPoints = new PointsForm(null); // Fase B
            dlgPoints.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dlgPoints.setVisible(true);

            CollectionsForm dlgCollections = new CollectionsForm(null); // Fase C
            dlgCollections.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dlgCollections.setVisible(true);
            */

            // 🔹 NUEVA LÍNEA PRINCIPAL (Fase D - Reportes)
            new com.umg.ecotrack.ui.ReportsForm(null).setVisible(true);

            // ----------------------------------------

            System.exit(0);
        });
    }
}
