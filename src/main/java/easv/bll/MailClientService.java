package easv.bll;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MailClientService {

    public void openDraft(String recipientEmail, String subject, String body) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email is required.");
        }

        try {
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.MAIL)) {
                throw new IllegalStateException("Opening the default mail app is not supported on this machine.");
            }

            String uri = "mailto:" + recipientEmail.trim()
                    + "?subject=" + encode(subject)
                    + "&body=" + encode(body);

            desktop.mail(new URI(uri));
        } catch (Exception ex) {
            throw new RuntimeException("Could not open the default mail app.", ex);
        }
    }

    private String encode(String value) {
        String safeValue = value == null ? "" : value;
        return URLEncoder.encode(safeValue, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
