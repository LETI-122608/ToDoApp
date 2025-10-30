package com.example.examplefeature.exportpdf;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class DownloadPdfView {

    private final PdfExportService pdfExportService;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl; // Prefer this for QR links

    public DownloadPdfView(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    @GetMapping(value = "/download-pdf", produces = "application/pdf")
    public void downloadPdf(
        //TODO
            @RequestParam(name = "disposition", defaultValue = "attachment") String disposition,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String base = resolveBaseUrl(request);
        // The QR in the PDF should always cause a download
        String qrUrl = base + "/download-pdf?disposition=attachment";
        System.out.println("Resolved base URL for QR: " + resolveBaseUrl(request));

        byte[] pdfBytes = pdfExportService.exportTasksToPdfBytes(qrUrl);

        response.setContentType("application/pdf");
        String cdType = "inline".equalsIgnoreCase(disposition) ? "inline" : "attachment";
        response.setHeader("Content-Disposition", cdType + "; filename=\"tasks.pdf\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentLength(pdfBytes.length);

        try (ServletOutputStream os = response.getOutputStream()) {
            os.write(pdfBytes);
        }
    }

    /** Prefer configured base; else derive from HttpServletRequest with X-Forwarded support */
    private String resolveBaseUrl(HttpServletRequest req) {
        String configured = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (!configured.isEmpty()) return configured.replaceAll("/+$", "");

        // Honor reverse-proxy headers if present
        String scheme = headerOr(req, "X-Forwarded-Proto", req.getScheme());
        String host   = headerOr(req, "X-Forwarded-Host",  req.getServerName());
        String portS  = headerOr(req, "X-Forwarded-Port",  String.valueOf(req.getServerPort()));
        String ctx    = req.getContextPath();

        if (host.contains(":")) return scheme + "://" + host + ctx;

        int port = Integer.parseInt(portS);
        boolean def = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + host + (def ? "" : ":" + port) + ctx;
    }

    private static String headerOr(HttpServletRequest r, String name, String fallback) {
        String v = r.getHeader(name);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
