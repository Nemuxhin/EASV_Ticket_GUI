package Java.dal;

import Java.be.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    private static final List<Ticket> TICKETS = new ArrayList<>();

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(TICKETS);
    }

    public void addTicket(Ticket ticket) {
        TICKETS.add(ticket);
    }
}