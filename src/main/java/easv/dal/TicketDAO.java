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

    public void addTickets(List<Ticket> ticketsToAdd) {
        if (ticketsToAdd == null || ticketsToAdd.isEmpty()) {
            return;
        }

        for (Ticket ticket : ticketsToAdd) {
            addTicket(ticket);
        }
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

    public Ticket findByTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            return null;
        }

        for (Ticket ticket : tickets) {
            if (ticketId.equals(ticket.getTicketId())) {
                return ticket;
            }
        }
        return null;
    }

    public List<Ticket> findByCustomerEmail(String customerEmail) {
        List<Ticket> result = new ArrayList<>();

        if (customerEmail == null || customerEmail.isBlank()) {
            return result;
        }

        for (Ticket ticket : tickets) {
            if (ticket.hasCustomer()
                    && customerEmail.equalsIgnoreCase(ticket.getCustomer().getEmail())) {
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