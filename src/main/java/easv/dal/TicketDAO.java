package easv.dal;

import easv.be.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    private static final List<Ticket> tickets = new ArrayList<>();

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets);
    }

    public Ticket findByToken(String secureToken) {
        for (Ticket ticket : tickets) {
            if (ticket.getSecureToken().equals(secureToken)) {
                return ticket;
            }
        }
        return null;
    }
}