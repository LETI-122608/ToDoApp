package com.example.examplefeature.exportpdf;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DownloadPdfView {

    private final PdfExportService pdfExportService;

    public DownloadPdfView(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    @GetMapping(value = "/download-pdf", produces = "application/pdf")
    public void downloadPdf(HttpServletResponse response) throws IOException {
        byte[] pdfBytes = pdfExportService.exportTasksToPdfBytes();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"tasks.pdf\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentLength(pdfBytes.length);

        try (ServletOutputStream os = response.getOutputStream()) {
            os.write(pdfBytes);
            os.flush();
        }
    }
}
