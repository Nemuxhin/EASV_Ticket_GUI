package Java.bll;

import Java.be.Ticket;
import Java.dal.TicketDAO;

import java.util.List;

public class TicketManager {

    private final TicketDAO ticketDAO = new TicketDAO();

    public List<Ticket> getTickets() {
        return ticketDAO.getAllTickets();
    }

    public void createTicket(Ticket ticket) {
        ticketDAO.addTicket(ticket);
    }
}