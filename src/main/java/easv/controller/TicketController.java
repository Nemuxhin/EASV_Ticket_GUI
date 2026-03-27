package easv.controller;

import easv.be.Ticket;
import easv.bll.TicketManager;

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