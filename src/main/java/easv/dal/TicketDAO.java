package easv.dal;

import easv.be.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    private static final List<Ticket> tickets = new ArrayList<>();

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
}