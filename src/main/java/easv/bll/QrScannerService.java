package easv.bll;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class QrScannerService {

    public String decodeToken(File imageFile) {
        if (imageFile == null || !imageFile.isFile()) {
            throw new IllegalArgumentException("Please choose a valid image file.");
        }

        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                throw new IllegalArgumentException("The selected file could not be read as an image.");
            }

            BinaryBitmap bitmap = new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(image))
            );

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception ex) {
            throw new RuntimeException("Could not decode a QR/barcode from the selected image.", ex);
        }
    }
}
