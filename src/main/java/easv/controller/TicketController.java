package easv.controller;

import easv.be.Customer;
import easv.be.Event;
import easv.be.SoldTicketRecord;
import easv.be.SpecialTicketRecord;
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

    public void createSpecialTicketDefinition(String eventTitle,
                                              String ticketType,
                                              String ticketDescription,
                                              String price,
                                              int quantity,
                                              boolean validForAllEvents) {
        ticketManager.createSpecialTicketDefinition(
                eventTitle,
                ticketType,
                ticketDescription,
                price,
                quantity,
                validForAllEvents
        );
    }

    public List<Ticket> issueSpecialTicketDefinition(SpecialTicketRecord definition, Event event) {
        return ticketManager.issueSpecialTicketDefinition(definition, event);
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

    public List<SpecialTicketRecord> getSpecialTicketRecords() {
        return ticketManager.getSpecialTicketRecords();
    }

    public List<SoldTicketRecord> getSoldTickets() {
        return ticketManager.getSoldTickets();
    }

    public List<SoldTicketRecord> getRecentSoldTickets(int limit) {
        return ticketManager.getRecentSoldTickets(limit);
    }

    public List<SoldTicketRecord> searchSoldTickets(String query, int limit) {
        return ticketManager.searchSoldTickets(query, limit);
    }

    public boolean deactivateSpecialTicketGroup(String ticketGroupId) {
        return ticketManager.deactivateSpecialTicketGroup(ticketGroupId);
    }

    public Ticket findByToken(String secureToken) {
        return ticketManager.findByToken(secureToken);
    }

    public Ticket findTicketByPublicCodeOrTicketId(String value) {
        return ticketManager.findTicketByPublicCodeOrTicketId(value);
    }

    public SoldTicketRecord findSoldTicketByPublicCodeOrTicketId(String value) {
        return ticketManager.findSoldTicketByPublicCodeOrTicketId(value);
    }

    public Ticket buildTicketFromSoldRecord(SoldTicketRecord soldTicket) {
        return ticketManager.buildTicketFromSoldRecord(soldTicket);
    }

    public boolean isTicketValid(String lookupValue) {
        return ticketManager.isTicketValid(lookupValue);
    }

    public boolean isTicketValid(String lookupValue, String eventTitle) {
        return ticketManager.isTicketValid(lookupValue, eventTitle);
    }

    public boolean markTicketAsUsed(String lookupValue) {
        return ticketManager.markTicketAsUsed(lookupValue);
    }

    public boolean setTicketUsedState(String ticketId, boolean used) {
        return ticketManager.setTicketUsedState(ticketId, used);
    }

    public boolean setSoldTicketUsedState(String publicCode, boolean used) {
        return ticketManager.setSoldTicketUsedState(publicCode, used);
    }

    public String getEventStatus(Event event) {
        return ticketManager.getEventStatus(event);
    }
}
