package Java.gui.controller;

import Java.Be.Ticket;
import Java.Bll.TicketManager;

import java.util.List;

public class TicketController {

    private final TicketManager ticketManager = new TicketManager();

    public List<Ticket> getTickets() {
        return ticketManager.getTickets();
    }

    public void createTicket(Ticket ticket) {
        ticketManager.createTicket(ticket);
    }
}