package com.example.examplefeature.exportpdf;

import com.example.examplefeature.exportpdf.PdfExportService;
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

    @GetMapping("/download-pdf")
    public void downloadPdf(HttpServletResponse response) throws IOException {
        byte[] pdfBytes = pdfExportService.exportTasksToPdfBytes();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.pdf");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}


