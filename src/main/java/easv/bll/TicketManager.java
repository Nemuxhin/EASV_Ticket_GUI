package easv.bll;

import easv.be.Customer;
import easv.be.Event;
import easv.be.Ticket;
import easv.dal.TicketDAO;

import java.util.ArrayList;
import java.util.List;

public class TicketManager {

    private final TicketDAO ticketDAO;
    private final TokenGenerator tokenGenerator;
    private final QrCodeGenerator qrCodeGenerator;
    private final BarcodeGenerator barcodeGenerator;

    public TicketManager() {
        this.ticketDAO = new TicketDAO();
        this.tokenGenerator = new TokenGenerator();
        this.qrCodeGenerator = new QrCodeGenerator();
        this.barcodeGenerator = new BarcodeGenerator();
    }

    public Ticket createEventTicket(Event event,
                                    Customer customer,
                                    String ticketType,
                                    String ticketDescription,
                                    String price,
                                    String endDateTime,
                                    String locationGuidance) {
        validateEventTicketInput(event, customer, ticketType, price);

        return buildAndSaveTicket(
                tokenGenerator.generateTicketId(),
                tokenGenerator.generateSecureToken(),
                customer,
                event.getTitle(),
                event.getStartDateTime(),
                endDateTime,
                event.getLocation(),
                locationGuidance,
                event.getNotes(),
                ticketType,
                ticketDescription,
                price,
                false,
                false
        );
    }

    public List<Ticket> createEventTickets(Event event,
                                           Customer customer,
                                           String ticketType,
                                           String ticketDescription,
                                           String pricePerTicket,
                                           String endDateTime,
                                           String locationGuidance,
                                           int quantity) {
        validateEventTicketInput(event, customer, ticketType, pricePerTicket);
        validateQuantity(quantity);

        List<Ticket> createdTickets = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            Ticket ticket = buildAndSaveTicket(
                    tokenGenerator.generateTicketId(),
                    tokenGenerator.generateSecureToken(),
                    customer,
                    event.getTitle(),
                    event.getStartDateTime(),
                    endDateTime,
                    event.getLocation(),
                    locationGuidance,
                    event.getNotes(),
                    ticketType,
                    ticketDescription,
                    pricePerTicket,
                    false,
                    false
            );

            createdTickets.add(ticket);
        }

        return createdTickets;
    }

    public Ticket createSpecialTicket(String eventTitle,
                                      String eventStartDateTime,
                                      String eventEndDateTime,
                                      String eventLocation,
                                      String eventLocationGuidance,
                                      String eventNotes,
                                      String ticketType,
                                      String ticketDescription,
                                      String price,
                                      boolean validForAllEvents) {
        validateSpecialTicketInput(eventTitle, ticketType, price, validForAllEvents);

        return buildAndSaveTicket(
                tokenGenerator.generateTicketId(),
                tokenGenerator.generateSecureToken(),
                null,
                normalizeEventTitle(eventTitle, validForAllEvents),
                eventStartDateTime,
                eventEndDateTime,
                eventLocation,
                eventLocationGuidance,
                eventNotes,
                ticketType,
                ticketDescription,
                price,
                true,
                validForAllEvents
        );
    }

    public List<Ticket> createSpecialTickets(String eventTitle,
                                             String eventStartDateTime,
                                             String eventEndDateTime,
                                             String eventLocation,
                                             String eventLocationGuidance,
                                             String eventNotes,
                                             String ticketType,
                                             String ticketDescription,
                                             String price,
                                             boolean validForAllEvents,
                                             int quantity) {
        validateSpecialTicketInput(eventTitle, ticketType, price, validForAllEvents);
        validateQuantity(quantity);

        List<Ticket> createdTickets = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            Ticket ticket = buildAndSaveTicket(
                    tokenGenerator.generateTicketId(),
                    tokenGenerator.generateSecureToken(),
                    null,
                    normalizeEventTitle(eventTitle, validForAllEvents),
                    eventStartDateTime,
                    eventEndDateTime,
                    eventLocation,
                    eventLocationGuidance,
                    eventNotes,
                    ticketType,
                    ticketDescription,
                    price,
                    true,
                    validForAllEvents
            );

            createdTickets.add(ticket);
        }

        return createdTickets;
    }

    private Ticket buildAndSaveTicket(String ticketId,
                                      String secureToken,
                                      Customer customer,
                                      String eventTitle,
                                      String eventStartDateTime,
                                      String eventEndDateTime,
                                      String eventLocation,
                                      String eventLocationGuidance,
                                      String eventNotes,
                                      String ticketType,
                                      String ticketDescription,
                                      String price,
                                      boolean specialTicket,
                                      boolean validForAllEvents) {

        byte[] qrImage = qrCodeGenerator.generateQrCode(secureToken);
        byte[] barcodeImage = barcodeGenerator.generateBarcode(secureToken);

        Ticket ticket = new Ticket(
                ticketId,
                secureToken,
                false,
                customer,
                eventTitle,
                eventStartDateTime,
                eventEndDateTime,
                eventLocation,
                eventLocationGuidance,
                eventNotes,
                ticketType,
                ticketDescription,
                price,
                specialTicket,
                validForAllEvents,
                qrImage,
                barcodeImage
        );

        ticketDAO.addTicket(ticket);
        return ticket;
    }

    public List<Ticket> getAllTickets() {
        return ticketDAO.getAllTickets();
    }

    public Ticket findByToken(String secureToken) {
        return ticketDAO.findByToken(secureToken);
    }

    public Ticket findByTicketId(String ticketId) {
        return ticketDAO.findByTicketId(ticketId);
    }

    public List<Ticket> getTicketsByCustomerEmail(String customerEmail) {
        return ticketDAO.findByCustomerEmail(customerEmail);
    }

    public boolean isTicketValid(String secureToken) {
        Ticket ticket = ticketDAO.findByToken(secureToken);
        return ticket != null && !ticket.isUsed();
    }

    public boolean isTicketValid(String secureToken, String eventTitle) {
        Ticket ticket = ticketDAO.findByToken(secureToken);

        if (ticket == null || ticket.isUsed()) {
            return false;
        }

        if (eventTitle == null || eventTitle.isBlank()) {
            return true;
        }

        return ticket.matchesEvent(eventTitle);
    }

    public boolean markTicketAsUsed(String secureToken) {
        Ticket ticket = ticketDAO.findByToken(secureToken);

        if (ticket == null || ticket.isUsed()) {
            return false;
        }

        ticket.setUsed(true);
        return ticketDAO.updateTicket(ticket);
    }

    private void validateEventTicketInput(Event event,
                                          Customer customer,
                                          String ticketType,
                                          String price) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        if (ticketType == null || ticketType.isBlank()) {
            throw new IllegalArgumentException("Ticket type cannot be blank.");
        }

        if (price == null || price.isBlank()) {
            throw new IllegalArgumentException("Price cannot be blank.");
        }
    }

    private void validateSpecialTicketInput(String eventTitle,
                                            String ticketType,
                                            String price,
                                            boolean validForAllEvents) {
        if (!validForAllEvents && (eventTitle == null || eventTitle.isBlank())) {
            throw new IllegalArgumentException("Event title is required unless the ticket is valid for all events.");
        }

        if (ticketType == null || ticketType.isBlank()) {
            throw new IllegalArgumentException("Ticket type cannot be blank.");
        }

        if (price == null || price.isBlank()) {
            throw new IllegalArgumentException("Price cannot be blank.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
    }

    private String normalizeEventTitle(String eventTitle, boolean validForAllEvents) {
        if (validForAllEvents && (eventTitle == null || eventTitle.isBlank())) {
            return "All Events";
        }
        return eventTitle;
    }
}