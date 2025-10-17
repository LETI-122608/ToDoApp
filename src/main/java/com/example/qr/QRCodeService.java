package com.example.qr;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class QRCodeService{

    public byte[] toPng(String text, int sizePx) {
        String data = (text == null || text.isBlank()) ? "(empty)" : text;
        int size = Math.max(128, Math.min(sizePx, 1024)); // guardrails

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // 15% EC
        hints.put(EncodeHintType.MARGIN, 1);                                // quiet zone
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());

        try {
            BitMatrix m = new MultiFormatWriter()
                    .encode(data, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(m);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("QR encode failed", e);
        }
    }

    public InputStream asStream(String text, int sizePx) {
        return new ByteArrayInputStream(toPng(text, sizePx));
    }
}


