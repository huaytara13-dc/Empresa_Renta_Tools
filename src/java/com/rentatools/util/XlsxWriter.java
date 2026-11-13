package com.rentatools.util;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Genera un archivo Excel (.xlsx) sin librerias externas: un .xlsx es un ZIP con
 * varios XML. Escribe una sola hoja con encabezado en negrita.
 */
public final class XlsxWriter {

    private XlsxWriter() { }

    public static void escribir(OutputStream out, String nombreHoja, String titulo,
                                List<String> columnas, List<List<Object>> filas) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            entrada(zip, "[Content_Types].xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
              + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
              + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
              + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
              + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
              + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
              + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
              + "</Types>");
            entrada(zip, "_rels/.rels",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
              + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
              + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
              + "</Relationships>");
            entrada(zip, "xl/workbook.xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
              + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
              + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
              + "<sheets><sheet name=\"" + xml(limpiarHoja(nombreHoja)) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            entrada(zip, "xl/_rels/workbook.xml.rels",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
              + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
              + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
              + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
              + "</Relationships>");
            // estilo 0 = normal, 1 = negrita con fondo, 2 = titulo grande
            entrada(zip, "xl/styles.xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
              + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
              + "<fonts count=\"3\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
              + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
              + "<font><b/><sz val=\"14\"/><name val=\"Calibri\"/></font></fonts>"
              + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>"
              + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1F5FBF\"/></patternFill></fill></fills>"
              + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
              + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
              + "<cellXfs count=\"3\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
              + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
              + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/></cellXfs>"
              + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
              + "</styleSheet>");

            StringBuilder s = new StringBuilder(8192);
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
             .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
             .append("<cols><col min=\"1\" max=\"").append(Math.max(1, columnas.size()))
             .append("\" width=\"24\" customWidth=\"1\"/></cols><sheetData>");
            int fila = 1;
            s.append("<row r=\"1\">").append(texto(1, 1, titulo, 2)).append("</row>");
            fila = 3;
            s.append("<row r=\"").append(fila).append("\">");
            for (int c = 0; c < columnas.size(); c++) {
                s.append(texto(c + 1, fila, columnas.get(c), 1));
            }
            s.append("</row>");
            for (List<Object> f : filas) {
                fila++;
                s.append("<row r=\"").append(fila).append("\">");
                for (int c = 0; c < f.size(); c++) {
                    Object v = f.get(c);
                    if (v == null) {
                        continue;
                    }
                    if (v instanceof Number) {
                        s.append("<c r=\"").append(ref(c + 1, fila)).append("\"><v>")
                         .append(new BigDecimal(v.toString()).toPlainString()).append("</v></c>");
                    } else {
                        s.append(texto(c + 1, fila, v.toString(), 0));
                    }
                }
                s.append("</row>");
            }
            s.append("</sheetData></worksheet>");
            entrada(zip, "xl/worksheets/sheet1.xml", s.toString());
        }
    }

    private static String texto(int col, int fila, String valor, int estilo) {
        return "<c r=\"" + ref(col, fila) + "\" t=\"inlineStr\" s=\"" + estilo + "\"><is><t xml:space=\"preserve\">"
             + xml(valor) + "</t></is></c>";
    }

    private static String ref(int col, int fila) {
        StringBuilder l = new StringBuilder();
        int n = col;
        while (n > 0) {
            int r = (n - 1) % 26;
            l.insert(0, (char) ('A' + r));
            n = (n - 1) / 26;
        }
        return l.toString() + fila;
    }

    private static String limpiarHoja(String n) {
        String s = n == null ? "Reporte" : n.replaceAll("[\\\\/?*\\[\\]:]", " ").trim();
        if (s.isEmpty()) s = "Reporte";
        return s.length() > 31 ? s.substring(0, 31) : s;
    }

    private static String xml(String s) {
        StringBuilder b = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&': b.append("&amp;"); break;
                case '<': b.append("&lt;"); break;
                case '>': b.append("&gt;"); break;
                case '"': b.append("&quot;"); break;
                default:
                    if (ch >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') b.append(ch);
            }
        }
        return b.toString();
    }

    private static void entrada(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
