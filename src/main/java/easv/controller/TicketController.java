package easv.controller;

import easv.be.Customer;
import easv.be.Event;
import easv.be.Ticket;
import easv.bll.TicketManager;

public class TicketController {

    private final TicketManager ticketManager;

    public TicketController() {
        this.ticketManager = new TicketManager();
    }

    public Ticket createEventTicket(Event event,
                                    Customer customer,
                                    String ticketType,
                                    String ticketDescription,
                                    String price,
                                    String endDateTime,
                                    String locationGuidance) {
        return ticketManager.createEventTicket(
                event,
                customer,
                ticketType,
                ticketDescription,
                price,
                endDateTime,
                locationGuidance
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
        return ticketManager.createSpecialTicket(
                eventTitle,
                eventStartDateTime,
                eventEndDateTime,
                eventLocation,
                eventLocationGuidance,
                eventNotes,
                ticketType,
                ticketDescription,
                price,
                validForAllEvents
        );
    }

    public Ticket findByToken(String secureToken) {
        return ticketManager.findByToken(secureToken);
    }

    public boolean isTicketValid(String secureToken) {
        return ticketManager.isTicketValid(secureToken);
    }

    public boolean markTicketAsUsed(String secureToken) {
        return ticketManager.markTicketAsUsed(secureToken);
    }
}