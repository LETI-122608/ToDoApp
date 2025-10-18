package com.example.examplefeature.exportpdf;

import com.example.base.ui.component.ViewToolbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;

@Route("export-pdf")
@PageTitle("Export PDF")
@Menu(order = 1, icon = "vaadin:file-presentation", title = "Export PDF")
public class ExportPdfPreviewView extends Main {

    public ExportPdfPreviewView() {
        String ctx = VaadinService.getCurrentRequest() != null
                ? VaadinService.getCurrentRequest().getContextPath()
                : "";

        String previewUrl  = ctx + "/download-pdf?disposition=inline";
        String downloadUrl = ctx + "/download-pdf";

        // Top toolbar with a Download button
        Button downloadBtn = new Button("Download PDF", e -> UI.getCurrent().getPage().open(downloadUrl));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshBtn = new Button("Refresh preview", e -> {
            // Bust any caches and regenerate
            UI.getCurrent().getPage().executeJs(
                    "const f = document.getElementById($0); f.src = $1 + '&_ts=' + Date.now();",
                    "pdf-frame", previewUrl);
        });

        add(new ViewToolbar("Export PDF", ViewToolbar.group(downloadBtn, refreshBtn)));

        // Inline PDF preview
        IFrame pdfFrame = new IFrame(previewUrl);
        pdfFrame.setId("pdf-frame");
        pdfFrame.setWidth("100%");
        pdfFrame.setHeight("85vh");
        pdfFrame.getElement().setAttribute("type", "application/pdf");
        pdfFrame.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");

        add(pdfFrame);
    }
}
