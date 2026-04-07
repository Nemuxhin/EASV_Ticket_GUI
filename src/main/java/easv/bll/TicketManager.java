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
        this(new TicketDAO(), new TokenGenerator(), new QrCodeGenerator(), new BarcodeGenerator());
    }

    public TicketManager(TicketDAO ticketDAO,
                         TokenGenerator tokenGenerator,
                         QrCodeGenerator qrCodeGenerator,
                         BarcodeGenerator barcodeGenerator) {
        this.ticketDAO = ticketDAO;
        this.tokenGenerator = tokenGenerator;
        this.qrCodeGenerator = qrCodeGenerator;
        this.barcodeGenerator = barcodeGenerator;
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

        return buildAndSaveTicket(
                ticketId,
                secureToken,
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
                false
        );
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

        return buildAndSaveTicket(
                ticketId,
                secureToken,
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
                validForAllEvents
        );
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
        // also persist to DAO here if you have an update method
        return true;
    }
}