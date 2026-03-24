package Java.bll;

import Java.be.Event;
import Java.be.Ticket;
import Java.dal.TicketDAO;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketManager {

    private final TicketDAO ticketDAO;

    public TicketManager() {
        this.ticketDAO = new TicketDAO();
    }

    public List<Ticket> generateEntryTickets(Event event, Event.TicketOption option, int amount, String description) {
        List<Ticket> tickets = new ArrayList<>();

        if (event == null || option == null || amount <= 0) {
            return tickets;
        }

        for (int i = 0; i < amount; i++) {
            String secureCode = generateSecureCode();

            Ticket ticket = new Ticket(
                    UUID.randomUUID().toString(),
                    secureCode,
                    generateQrCodeBytes(secureCode),
                    generate1DBarcodeBytes(secureCode),
                    event,
                    false,
                    Ticket.KIND_ENTRY,
                    option.getName(),
                    option.getPriceText(),
                    buildDescription(option.getDescription(), description),
                    false
            );

            tickets.add(ticket);
        }

        ticketDAO.addTickets(tickets);
        return tickets;
    }

    public Ticket generateBenefitTicket(String ticketName, String valueText, String description,
                                        Event event, boolean validForAllEvents) {
        String secureCode = generateSecureCode();

        Ticket ticket = new Ticket(
                UUID.randomUUID().toString(),
                secureCode,
                generateQrCodeBytes(secureCode),
                generate1DBarcodeBytes(secureCode),
                validForAllEvents ? null : event,
                validForAllEvents,
                Ticket.KIND_BENEFIT,
                ticketName,
                valueText,
                description,
                false
        );

        ticketDAO.addTicket(ticket);
        return ticket;
    }

    public List<Ticket> getAllTickets() {
        return ticketDAO.getAllTickets();
    }

    public boolean redeemTicket(String secureCode) {
        Ticket ticket = ticketDAO.findBySecureCode(secureCode);

        if (ticket == null || ticket.isUsed()) {
            return false;
        }

        ticket.setUsed(true);
        return true;
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    private String generateSecureCode() {
        return UUID.randomUUID().toString();
    }

    // These are placeholders so the architecture stays correct.
    // Replace with a real QR / barcode library later.
    private byte[] generateQrCodeBytes(String value) {
        return ("QR:" + value).getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generate1DBarcodeBytes(String value) {
        return ("BAR:" + value).getBytes(StandardCharsets.UTF_8);
    }

    private String buildDescription(String baseDescription, String extraDescription) {
        if ((baseDescription == null || baseDescription.isBlank()) &&
                (extraDescription == null || extraDescription.isBlank())) {
            return "";
        }

        if (baseDescription == null || baseDescription.isBlank()) {
            return extraDescription;
        }

        if (extraDescription == null || extraDescription.isBlank()) {
            return baseDescription;
        }

        return baseDescription + " | " + extraDescription;
    }
}