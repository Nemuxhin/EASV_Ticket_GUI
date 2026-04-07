package easv.bll;

import easv.be.Customer;
import easv.be.Event;
import easv.be.Ticket;
import easv.dal.TicketDAO;

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

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        String ticketId = tokenGenerator.generateTicketId();
        String secureToken = tokenGenerator.generateSecureToken();
        byte[] qrImage = qrCodeGenerator.generateQrCode(secureToken);
        byte[] barcodeImage = barcodeGenerator.generateBarcode(secureToken);

        Ticket ticket = new Ticket(
                ticketId,
                secureToken,
                false,
                customer,
                event.getTitle(),
                event.getDate(),
                endDateTime,
                event.getLocation(),
                locationGuidance,
                event.getNotes(),
                ticketType,
                ticketDescription,
                price,
                false,
                false,
                qrImage,
                barcodeImage
        );

        ticketDAO.addTicket(ticket);
        return ticket;
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

        String ticketId = tokenGenerator.generateTicketId();
        String secureToken = tokenGenerator.generateSecureToken();
        byte[] qrImage = qrCodeGenerator.generateQrCode(secureToken);
        byte[] barcodeImage = barcodeGenerator.generateBarcode(secureToken);

        Ticket ticket = new Ticket(
                ticketId,
                secureToken,
                false,
                null,
                eventTitle,
                eventStartDateTime,
                eventEndDateTime,
                eventLocation,
                eventLocationGuidance,
                eventNotes,
                ticketType,
                ticketDescription,
                price,
                true,
                validForAllEvents,
                qrImage,
                barcodeImage
        );

        ticketDAO.addTicket(ticket);
        return ticket;
    }

    public Ticket findByToken(String secureToken) {
        return ticketDAO.findByToken(secureToken);
    }

    public boolean isTicketValid(String secureToken) {
        Ticket ticket = ticketDAO.findByToken(secureToken);
        return ticket != null && !ticket.isUsed();
    }

    public boolean markTicketAsUsed(String secureToken) {
        Ticket ticket = ticketDAO.findByToken(secureToken);

        if (ticket == null || ticket.isUsed()) {
            return false;
        }

        ticket.setUsed(true);
        return ticketDAO.markTicketAsUsed(secureToken);
    }
}
