package easv.dal;

import easv.be.Ticket;
import easv.be.Event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TicketDAO {

    private static final List<Ticket> tickets = new ArrayList<>();
    private static final Map<String, LinkedHashMap<String, String>> eventTicketTypes = new LinkedHashMap<>();

    public void addTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null.");
        }
        tickets.add(ticket);
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets);
    }

    public Ticket findByToken(String secureToken) {
        if (secureToken == null || secureToken.isBlank()) {
            return null;
        }

        for (Ticket ticket : tickets) {
            if (secureToken.equals(ticket.getSecureToken())) {
                return ticket;
            }
        }
        return null;
    }

    public List<Ticket> findByGroupId(String ticketGroupId) {
        List<Ticket> result = new ArrayList<>();

        if (ticketGroupId == null || ticketGroupId.isBlank()) {
            return result;
        }

        for (Ticket ticket : tickets) {
            if (ticketGroupId.equals(ticket.getTicketGroupId())) {
                result.add(ticket);
            }
        }

        return result;
    }

    public boolean updateTicket(Ticket updatedTicket) {
        if (updatedTicket == null || updatedTicket.getTicketId() == null) {
            return false;
        }

        for (int i = 0; i < tickets.size(); i++) {
            Ticket currentTicket = tickets.get(i);

            if (updatedTicket.getTicketId().equals(currentTicket.getTicketId())) {
                tickets.set(i, updatedTicket);
                return true;
            }
        }

        return false;
    }

    public LinkedHashMap<String, String> getTicketTypesForEvent(Event event) {
        if (event == null) {
            return new LinkedHashMap<>();
        }

        LinkedHashMap<String, String> storedTypes = eventTicketTypes.get(buildEventKey(event));
        if (storedTypes == null) {
            return new LinkedHashMap<>();
        }

        return new LinkedHashMap<>(storedTypes);
    }

    public void setTicketTypesForEvent(Event event, LinkedHashMap<String, String> ticketTypes) {
        if (event == null) {
            return;
        }

        if (ticketTypes == null || ticketTypes.isEmpty()) {
            eventTicketTypes.remove(buildEventKey(event));
            return;
        }

        eventTicketTypes.put(buildEventKey(event), new LinkedHashMap<>(ticketTypes));
    }

    public void removeTicketTypesForEvent(Event event) {
        if (event == null) {
            return;
        }

        eventTicketTypes.remove(buildEventKey(event));
    }

    public void moveTicketTypesToUpdatedEvent(Event oldEvent, Event updatedEvent) {
        if (oldEvent == null || updatedEvent == null) {
            return;
        }

        String oldKey = buildEventKey(oldEvent);
        LinkedHashMap<String, String> types = eventTicketTypes.remove(oldKey);
        if (types == null) {
            return;
        }

        eventTicketTypes.put(buildEventKey(updatedEvent), types);
    }

    private String buildEventKey(Event event) {
        String title = event.getTitle() == null ? "" : event.getTitle().trim().toLowerCase();
        String location = event.getLocation() == null ? "" : event.getLocation().trim().toLowerCase();
        String start = event.getStartDateTime() == null ? "" : event.getStartDateTime().trim().toLowerCase();
        return title + "|" + location + "|" + start;
    }
}
