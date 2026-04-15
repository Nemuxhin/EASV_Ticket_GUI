package easv.bll;

import easv.be.Customer;
import easv.be.Ticket;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TicketPdfService {

    public byte[] createTicketsPdf(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one ticket to export.");
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            for (Ticket ticket : tickets) {
                addTicketPage(document, ticket);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Could not generate ticket PDF.", ex);
        }
    }

    public Path saveTicketsPdf(List<Ticket> tickets, Path outputPath) {
        try {
            Files.write(outputPath, createTicketsPdf(tickets));
            return outputPath;
        } catch (IOException ex) {
            throw new RuntimeException("Could not save ticket PDF.", ex);
        }
    }

    public Path createTempPdf(List<Ticket> tickets) {
        try {
            Path tempFile = Files.createTempFile("tickets-", ".pdf");
            return saveTicketsPdf(tickets, tempFile);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create temporary PDF file.", ex);
        }
    }

    public void printPdf(Path pdfPath) {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.PRINT)) {
                desktop.print(pdfPath.toFile());
            } else {
                desktop.open(pdfPath.toFile());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not print/open the PDF file.", ex);
        }
    }

    private void addTicketPage(PDDocument document, Ticket ticket) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            float y = 790;

            write(content, 50, y, PDType1Font.HELVETICA_BOLD, 22, "EASV Ticket");
            y -= 34;

            write(content, 50, y, PDType1Font.HELVETICA_BOLD, 17, clean(ticket.getEventTitle()));
            y -= 28;

            y = labelValue(content, 50, y, "Ticket ID:", clean(ticket.getTicketId()));
            y = labelValue(content, 50, y, "Ticket Type:", clean(ticket.getTicketType()));
            y = labelValue(content, 50, y, "Price:", clean(ticket.getPrice()));
            y = labelValue(content, 50, y, "Start:", clean(ticket.getEventStartDateTime()));
            y = labelValue(content, 50, y, "End:", clean(ticket.getEventEndDateTime()));
            y = labelValue(content, 50, y, "Location:", clean(ticket.getEventLocation()));
            y = labelValue(content, 50, y, "Guidance:", clean(ticket.getEventLocationGuidance()));
            y = labelValue(content, 50, y, "Notes:", clean(ticket.getEventNotes()));

            Customer customer = ticket.getCustomer();
            if (customer != null) {
                y -= 8;
                y = labelValue(content, 50, y, "Customer:", clean(customer.getName()));
                y = labelValue(content, 50, y, "Email:", clean(customer.getEmail()));
            }

            if (ticket.getQrImage() != null) {
                PDImageXObject qrImage = PDImageXObject.createFromByteArray(document, ticket.getQrImage(), "qr-code");
                content.drawImage(qrImage, 50, 330, 150, 150);
            }

            if (ticket.getBarcodeImage() != null) {
                PDImageXObject barcodeImage = PDImageXObject.createFromByteArray(document, ticket.getBarcodeImage(), "barcode");
                content.drawImage(barcodeImage, 230, 360, 300, 80);
            }

            write(content, 50, 300, PDType1Font.HELVETICA_OBLIQUE, 11, "Scan this code at the venue.");
            write(content, 50, 284, PDType1Font.HELVETICA_OBLIQUE, 11, "This ticket can only be used once.");
        }
    }

    private float labelValue(PDPageContentStream content, float x, float y, String label, String value) throws IOException {
        write(content, x, y, PDType1Font.HELVETICA_BOLD, 12, label);
        write(content, x + 95, y, PDType1Font.HELVETICA, 12, value);
        return y - 18;
    }

    private void write(PDPageContentStream content, float x, float y, PDFont font, int size, String text) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(text == null ? "-" : text);
        content.endText();
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.replace("\r", " ").replace("\n", " ").trim();
    }
}
