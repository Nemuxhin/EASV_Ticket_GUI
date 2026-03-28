package easv.bll;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class BarcodeGenerator {

    public byte[] generateBarcode(String value) {
        try {
            Code128Writer writer = new Code128Writer();
            BitMatrix matrix = writer.encode(value, BarcodeFormat.CODE_128, 420, 120);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Could not generate barcode.", e);
        }
    }
}