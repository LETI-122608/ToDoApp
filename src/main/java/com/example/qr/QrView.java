package com.example.qr;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;

public class QrView extends VerticalLayout {
    private final QRCodeService qr = new QRCodeService();

    public QrView() {
        setWidthFull();
        H2 title = new H2("QR code generator");
        TextField input = new TextField("Text or URL");
        input.setWidthFull();

        IntegerField size = new IntegerField("Size (px)");
        size.setMin(128); size.setMax(1024); size.setValue(256);

        Image img = new Image(); img.setAlt("QR code");
        Anchor download = new Anchor(); download.setText("Download PNG");
        download.getElement().setAttribute("download", true); download.setVisible(false);

        Button generate = new Button("Generate", e -> {
            byte[] png = qr.toPng(input.getValue(), size.getValue());
            StreamResource res = new StreamResource("qrcode.png", () -> new ByteArrayInputStream(png));
            res.setCacheTime(0); // avoid stale images
            img.setSrc(res);
            img.setWidth(size.getValue() + "px"); img.setHeight(size.getValue() + "px");
            download.setHref(res); download.setVisible(true);
        });

        add(title, input, size, generate, img, download);
    }
}
