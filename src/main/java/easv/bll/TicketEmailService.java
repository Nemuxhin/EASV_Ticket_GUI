package easv.bll;

import easv.be.Ticket;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class TicketEmailService {

    private final TicketPdfService pdfService;

    public TicketEmailService() {
        this.pdfService = new TicketPdfService();
    }

    public void sendTickets(String recipientName, String recipientEmail, List<Ticket> tickets) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email is required.");
        }

        if (tickets == null || tickets.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one ticket to email.");
        }

        Properties config = loadMailConfig();
        String username = config.getProperty("mail.smtp.username");
        String password = config.getProperty("mail.smtp.password");
        String from = config.getProperty("mail.from", username);

        Properties sessionProps = new Properties();
        sessionProps.put("mail.smtp.host", config.getProperty("mail.smtp.host"));
        sessionProps.put("mail.smtp.port", config.getProperty("mail.smtp.port"));
        sessionProps.put("mail.smtp.auth", config.getProperty("mail.smtp.auth", "true"));
        sessionProps.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable", "true"));

        Session session = Session.getInstance(sessionProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Path tempPdf = null;

        try {
            tempPdf = pdfService.createTempPdf(tickets);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your ticket(s) for " + safe(tickets.get(0).getEventTitle()), StandardCharsets.UTF_8.name());

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(
                    "Hi " + safeName(recipientName) + ",\n\n" +
                            "Your ticket PDF is attached.\n" +
                            "Please bring it with you to the event.\n\n" +
                            "Best regards,\nEASV Tickets",
                    StandardCharsets.UTF_8.name()
            );

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(tempPdf.toFile());
            attachmentPart.setFileName("tickets.pdf");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);
            Transport.send(message);
        } catch (Exception ex) {
            throw new RuntimeException("Could not send ticket email.", ex);
        } finally {
            if (tempPdf != null) {
                try {
                    Files.deleteIfExists(tempPdf);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private Properties loadMailConfig() {
        try (InputStream inputStream = TicketEmailService.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("mail.properties was not found in resources.");
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (Exception ex) {
            throw new RuntimeException("Could not read mail configuration.", ex);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "event" : value.trim();
    }

    private String safeName(String value) {
        return value == null || value.isBlank() ? "there" : value.trim();
    }
}
