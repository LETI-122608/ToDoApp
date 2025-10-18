package com.example.examplefeature.exportpdf;

import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class PdfExportService {

    private final TaskService taskService;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PdfExportService(TaskService taskService) {
        this.taskService = taskService;
    }

    public byte[] exportTasksToPdfBytes() throws IOException {
        List<Task> tasks = taskService.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Page setup: A4, left-right=36pt, top=72pt (room for header), bottom=54pt (room for footer)
            Document document = new Document(PageSize.A4, 36f, 36f, 72f, 54f);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Fonts (embed Unicode-capable TTF; fallback to Helvetica if not found)
            BaseFont base = loadUnicodeBaseFont();
            Font titleFont  = new Font(base, 16f, Font.BOLD);
            Font metaFont   = new Font(base,  9f, Font.NORMAL, Color.GRAY);
            Font headerFont = new Font(base, 10f, Font.BOLD, Color.WHITE);
            Font cellFont   = new Font(base, 10f, Font.NORMAL, Color.BLACK);
            Font footerFont = new Font(base,  8f, Font.NORMAL, Color.GRAY);

            // Footer: "Página X de Y"
            writer.setPageEvent(new PageXofY(footerFont));

            // Metadata
            document.addTitle("Lista de Tarefas");
            document.addAuthor("ExampleFeature");
            document.addCreator("ExampleFeature");
            document.addSubject("Exportação de tarefas");
            document.addCreationDate();

            document.open();

            // Title
            Paragraph title = new Paragraph("Lista de Tarefas", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4f);
            document.add(title);

            // Meta line
            Paragraph meta = new Paragraph(
                    "Gerado em " + formatNow() + " • Total: " + tasks.size(),
                    metaFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(12f);
            document.add(meta);

            // Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{1f, 6f, 2.5f, 2.5f});
            table.setHeaderRows(1);

            Color headerBg = new Color(45, 55, 72); // dark slate
            table.addCell(th("#",          headerFont, headerBg, Element.ALIGN_CENTER));
            table.addCell(th("Descrição",  headerFont, headerBg, Element.ALIGN_LEFT));
            table.addCell(th("Prazo",      headerFont, headerBg, Element.ALIGN_CENTER));
            table.addCell(th("Criada",     headerFont, headerBg, Element.ALIGN_CENTER));

            if (tasks.isEmpty()) {
                PdfPCell empty = td("Nenhuma tarefa.", cellFont, Element.ALIGN_CENTER, Color.WHITE);
                empty.setColspan(4);
                table.addCell(empty);
            } else {
                for (int i = 0; i < tasks.size(); i++) {
                    Task t = tasks.get(i);
                    Color rowBg = (i % 2 == 0) ? new Color(250, 250, 250) : Color.WHITE;

                    table.addCell(td(String.valueOf(i + 1), cellFont, Element.ALIGN_CENTER, rowBg));
                    table.addCell(td(safe(t.getDescription()), cellFont, Element.ALIGN_LEFT, rowBg));
                    table.addCell(td(formatDate(t.getDueDate()), cellFont, Element.ALIGN_CENTER, rowBg));
                    table.addCell(td(formatDate(t.getCreationDate()), cellFont, Element.ALIGN_CENTER, rowBg));
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Erro a criar PDF: " + e.getMessage(), e);
        }
    }

    // ----- helpers -----

    private static PdfPCell th(String text, Font font, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setPadding(6f);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBorderWidth(0.5f);
        return c;
    }

    private static PdfPCell td(String text, Font font, int align, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setPadding(5f);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_TOP);
        c.setBackgroundColor(bg);
        c.setBorderWidth(0.5f);
        return c;
    }

    private static String formatNow() {
        return LocalDate.now().format(DATE_FMT);
    }

    // Accepts java.time.* or java.util.Date; otherwise falls back to toString()
    private static String formatDate(Object date) {
        if (date == null) return "-";
        if (date instanceof LocalDate) {
            return ((LocalDate) date).format(DATE_FMT);
        }
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).toLocalDate().format(DATE_FMT);
        }
        if (date instanceof Date) {
            Instant i = ((Date) date).toInstant();
            return i.atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_FMT);
        }
        return String.valueOf(date);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    // Load Unicode TTF from classpath; fallback to Helvetica if missing
    private static BaseFont loadUnicodeBaseFont() throws IOException, DocumentException {
        try (InputStream is = PdfExportService.class.getResourceAsStream("/fonts/DejaVuSans.ttf")) {
            if (is != null) {
                byte[] ttf = is.readAllBytes();
                return BaseFont.createFont("DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, ttf, null);
            }
        }
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }

    // Footer: "Página X de Y"
    private static class PageXofY extends PdfPageEventHelper {
        private final Font font;
        private PdfTemplate total;

        private PageXofY(Font font) { this.font = font; }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase("Página " + writer.getPageNumber() + " de ", font);
            float x = document.right() - 36f;
            float y = document.bottom() - 18f; // below bottom margin
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footer, x, y, 0);
            cb.addTemplate(total, x, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber() - 1), font), 2, 2, 0);
        }
    }
}
