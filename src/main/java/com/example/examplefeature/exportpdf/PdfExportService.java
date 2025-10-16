package com.example.examplefeature.exportpdf;

import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfExportService {

    private final TaskService taskService;

    public PdfExportService(TaskService taskService) {
        this.taskService = taskService;
    }

    public byte[] exportTasksToPdfBytes() throws IOException {
        List<Task> tasks = taskService.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("TO-DO List"));
            document.add(new Paragraph(" "));

            for (Task task : tasks) {
                document.add(new Paragraph(
                        "- " + safe(task.getDescription())
                                + (task.getDueDate() != null ? " | Prazo: " + task.getDueDate() : "")
                                + " | Criada: " + task.getCreationDate()
                ));
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Erro a criar PDF: " + e.getMessage(), e);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

