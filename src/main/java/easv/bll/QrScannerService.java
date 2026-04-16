package easv.bll;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

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

            return decodeBufferedImage(image);
        } catch (Exception ex) {
            throw new RuntimeException("Could not decode a QR/barcode from the selected image.", ex);
        }
    }

    public String decodeTokenFromPdf(File pdfFile) {
        if (pdfFile == null || !pdfFile.isFile()) {
            throw new IllegalArgumentException("Please choose a valid PDF file.");
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);

            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, 200);

                try {
                    return decodeBufferedImage(image);
                } catch (Exception ignored) {
                }
            }

            throw new IllegalArgumentException("No QR code or barcode was found in the selected PDF.");
        } catch (Exception ex) {
            throw new RuntimeException("Could not decode a QR/barcode from the selected PDF.", ex);
        }
    }

    private String decodeBufferedImage(BufferedImage image) throws Exception {
        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(image))
        );

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }
}
