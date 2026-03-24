package Java.Bll;

import Java.Be.Ticket;
import Java.Dal.TicketDAO;

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