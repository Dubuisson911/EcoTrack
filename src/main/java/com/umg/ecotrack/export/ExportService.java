package com.umg.ecotrack.export;

import com.umg.ecotrack.model.ReportRow;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Servicio de exportación de reportes a CSV, XLSX y PDF.
 * pom.xml:
 *  - org.apache.pdfbox:pdfbox:2.0.30
 *  - org.apache.poi:poi-ooxml:5.2.5
 */
public class ExportService {

    private static final DecimalFormat DF = new DecimalFormat("#0.00");

    // =====================================================================
    // CSV (compatible con Excel en es-ES → separador ;)
    // =====================================================================
    public static void toCsv(List<ReportRow> rows, String path) throws IOException {
        final String SEP = ";";
        try (FileWriter fw = new FileWriter(path)) {
            fw.write("Mes" + SEP + "Tipo" + SEP + "TotalKg\n");
            for (ReportRow r : rows) {
                fw.write(escapeCsv(r.mes()) + SEP
                        + escapeCsv(r.tipo()) + SEP
                        + DF.format(r.totalKg()) + "\n");
            }
        }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(";") || s.contains("\"") || s.contains("\n");
        String v = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + v + "\"" : v;
    }

    // =====================================================================
    // EXCEL (XLSX) – Columnas reales, encabezado y total
    // =====================================================================
    public static void toXlsx(List<ReportRow> rows, String path) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reporte");

            // Estilos
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle numberStyle = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            numberStyle.setDataFormat(fmt.getFormat("#,##0.00"));

            // Encabezados
            String[] headers = {"Mes", "Tipo", "Total (kg)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Datos
            int r = 1;
            double total = 0.0;
            for (ReportRow row : rows) {
                Row x = sheet.createRow(r++);
                x.createCell(0).setCellValue(row.mes());
                x.createCell(1).setCellValue(row.tipo());
                Cell cTot = x.createCell(2);
                cTot.setCellValue(row.totalKg());
                cTot.setCellStyle(numberStyle);
                total += row.totalKg();
            }

            // Fila TOTAL
            Row totalRow = sheet.createRow(r);
            Cell tLab = totalRow.createCell(1);
            tLab.setCellValue("TOTAL");
            tLab.setCellStyle(headerStyle);
            Cell tVal = totalRow.createCell(2);
            tVal.setCellValue(total);
            tVal.setCellStyle(numberStyle);

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(path)) {
                wb.write(fos);
            }
        }
    }

    // =====================================================================
    // PDF – Tabla con cabecera, líneas, footer y columnas más separadas
    // =====================================================================
    public static void toPdf(List<ReportRow> rows, String title, String footerLeft, String footerRight, String path) throws IOException {
        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            final float margin = 50f;
            final float pageWidth = page.getMediaBox().getWidth();
            final float startY = page.getMediaBox().getHeight() - margin;

            // Posiciones de columnas (amplias)
            final float xMes   = margin;
            final float xTipo  = margin + 220;
            final float xTotal = margin + 470;

            // Espaciados
            final float GAP_AFTER_TITLE     = 46f;
            final float HEADER_LINE_DROP    = 8f;
            final float GAP_AFTER_HEADER    = 18f;
            final float ROW_BASELINE_TO_LINE= 10f;
            final float ROW_EXTRA_SPACE     = 14f;
            final float PAGE_BOTTOM_GUARD   = 84f;   // deja espacio para footer

            float y = startY;

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.setLineWidth(0.8f);

            // Footer (por página)
            drawFooter(cs, page, margin, pageWidth, footerLeft, footerRight);

            // Título
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(margin, y);
            cs.showText(title);
            cs.endText();
            y -= GAP_AFTER_TITLE;

            // Línea bajo el título
            drawLine(cs, margin, y, pageWidth - margin, y);
            y -= 12f;

            // Cabecera
            float headerBaseline = drawHeader(cs, y, xMes, xTipo, xTotal);
            float headerLineY = headerBaseline - HEADER_LINE_DROP;
            drawLine(cs, xMes, headerLineY, xTotal + 120, headerLineY);
            y = headerLineY - GAP_AFTER_HEADER;

            // Filas
            double total = 0.0;
            for (ReportRow r : rows) {
                if (y < margin + PAGE_BOTTOM_GUARD) {
                    // nueva página
                    cs.close();
                    page = new PDPage(PDRectangle.LETTER);
                    doc.addPage(page);
                    y = startY;
                    cs = new PDPageContentStream(doc, page);
                    cs.setLineWidth(0.8f);

                    // Footer de la página
                    drawFooter(cs, page, margin, pageWidth, footerLeft, footerRight);

                    // título pequeño
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    cs.newLineAtOffset(margin, y);
                    cs.showText(title);
                    cs.endText();
                    y -= 20f;
                    drawLine(cs, margin, y, pageWidth - margin, y);
                    y -= 12f;

                    // header
                    headerBaseline = drawHeader(cs, y, xMes, xTipo, xTotal);
                    headerLineY = headerBaseline - HEADER_LINE_DROP;
                    drawLine(cs, xMes, headerLineY, xTotal + 120, headerLineY);
                    y = headerLineY - GAP_AFTER_HEADER;
                }

                // Mes
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(xMes, y);
                cs.showText(r.mes());
                cs.endText();

                // Tipo
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(xTipo, y);
                cs.showText(r.tipo());
                cs.endText();

                // Total
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(xTotal, y);
                cs.showText(DF.format(r.totalKg()));
                cs.endText();

                // línea por fila + aire extra
                float rowLineY = y - ROW_BASELINE_TO_LINE;
                drawLine(cs, xMes, rowLineY, xTotal + 120, rowLineY);
                y = rowLineY - ROW_EXTRA_SPACE;

                total += r.totalKg();
            }

            // Fila TOTAL
            y -= 10f;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(xTipo, y);
            cs.showText("TOTAL");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(xTotal, y);
            cs.showText(DF.format(total));
            cs.endText();

            cs.close();
            doc.save(path);
        }
    }

    // ===== Helpers PDF =====

    private static void drawFooter(PDPageContentStream cs, PDPage page, float margin, float pageWidth,
                                   String left, String right) throws IOException {
        float y = 30f; // a 30pt del borde inferior
        // línea sobre el footer
        drawLine(cs, margin, y + 10f, pageWidth - margin, y + 10f);

        // izquierda
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 9);
        cs.newLineAtOffset(margin, y);
        cs.showText(left != null ? left : "");
        cs.endText();

        // derecha
        if (right != null && !right.isEmpty()) {
            float rx = pageWidth - margin - (right.length() * 4.7f); // cálculo simple
            if (rx < margin) rx = margin;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.newLineAtOffset(rx, y);
            cs.showText(right);
            cs.endText();
        }
    }

    /** Dibuja el texto de cabecera y devuelve la baseline (y) del header. */
    private static float drawHeader(PDPageContentStream cs, float y, float xMes, float xTipo, float xTotal) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(xMes, y);
        cs.showText("Mes");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(xTipo, y);
        cs.showText("Tipo");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(xTotal, y);
        cs.showText("Total (kg)");
        cs.endText();

        return y;
    }

    private static void drawLine(PDPageContentStream cs, float x1, float y1, float x2, float y2) throws IOException {
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }
}
