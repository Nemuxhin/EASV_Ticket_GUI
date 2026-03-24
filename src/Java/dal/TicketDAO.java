package Java.dal;

import Java.be.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    private static final List<Ticket> TICKETS = new ArrayList<>();

    public void addTicket(Ticket ticket) {
        TICKETS.add(ticket);
    }

    public void addTickets(List<Ticket> tickets) {
        TICKETS.addAll(tickets);
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(TICKETS);
    }

    public Ticket findBySecureCode(String secureCode) {
        for (Ticket ticket : TICKETS) {
            if (ticket.getSecureCode().equals(secureCode)) {
                return ticket;
            }
        }
        return null;
    }
}