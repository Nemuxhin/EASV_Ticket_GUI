package easv.controller;

import easv.be.Customer;
import easv.be.Event;
import easv.be.Ticket;
import easv.bll.TicketManager;

import java.util.LinkedHashMap;
import java.util.List;

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

    public List<Ticket> createEventTickets(Event event,
                                           Customer customer,
                                           String ticketType,
                                           String ticketDescription,
                                           String pricePerTicket,
                                           String endDateTime,
                                           String locationGuidance,
                                           int quantity) {
        return ticketManager.createEventTickets(
                event,
                customer,
                ticketType,
                ticketDescription,
                pricePerTicket,
                endDateTime,
                locationGuidance,
                quantity
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
        return ticketManager.createSpecialTickets(
                eventTitle,
                eventStartDateTime,
                eventEndDateTime,
                eventLocation,
                eventLocationGuidance,
                eventNotes,
                ticketType,
                ticketDescription,
                price,
                validForAllEvents,
                quantity
        );
    }

    public LinkedHashMap<String, String> getTicketTypePricesForEvent(Event event) {
        return ticketManager.getTicketTypePricesForEvent(event);
    }

    public LinkedHashMap<String, String> getConfiguredTicketTypesForEvent(Event event) {
        return ticketManager.getConfiguredTicketTypesForEvent(event);
    }

    public void setConfiguredTicketTypesForEvent(Event event, LinkedHashMap<String, String> ticketTypes) {
        ticketManager.setConfiguredTicketTypesForEvent(event, ticketTypes);
    }

    public void moveConfiguredTicketTypes(Event oldEvent, Event updatedEvent) {
        ticketManager.moveConfiguredTicketTypes(oldEvent, updatedEvent);
    }

    public void removeConfiguredTicketTypes(Event event) {
        ticketManager.removeConfiguredTicketTypes(event);
    }

    public List<Ticket> getAllTickets() {
        return ticketManager.getAllTickets();
    }

    public List<Ticket> getSpecialTickets() {
        return ticketManager.getSpecialTickets();
    }

    public boolean deactivateSpecialTicketGroup(String ticketGroupId) {
        return ticketManager.deactivateSpecialTicketGroup(ticketGroupId);
    }

    public Ticket findByToken(String secureToken) {
        return ticketManager.findByToken(secureToken);
    }

    public boolean isTicketValid(String secureToken) {
        return ticketManager.isTicketValid(secureToken);
    }

    public boolean isTicketValid(String secureToken, String eventTitle) {
        return ticketManager.isTicketValid(secureToken, eventTitle);
    }

    public boolean markTicketAsUsed(String secureToken) {
        return ticketManager.markTicketAsUsed(secureToken);
    }

    public boolean setTicketUsedState(String ticketId, boolean used) {
        return ticketManager.setTicketUsedState(ticketId, used);
    }
}
