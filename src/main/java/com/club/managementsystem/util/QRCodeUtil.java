package com.club.managementsystem.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class QRCodeUtil {

    public String generateQRCode(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] qrCodeBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    public String generateActivityCheckInQRCode(Integer activityId) throws WriterException, IOException {
        String qrContent = "CLUB_ACTIVITY_CHECKIN:" + activityId + ":" + System.currentTimeMillis();
        return generateQRCode(qrContent, 300, 300);
    }

    public String generateActivityCheckInQRCodeWithNumber(Integer activityId, String checkInNumber) throws WriterException, IOException {
        String qrContent = "CLUB_ACTIVITY_CHECKIN:" + activityId + ":" + checkInNumber + ":" + System.currentTimeMillis();
        System.out.println("生成二维码内容: " + qrContent);
        String result = generateQRCode(qrContent, 300, 300);
        System.out.println("二维码生成成功，长度: " + (result != null ? result.length() : 0));
        return result;
    }
}