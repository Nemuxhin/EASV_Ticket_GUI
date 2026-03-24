package Java.controller;

import Java.be.Event;
import Java.be.Ticket;
import Java.bll.TicketManager;

import java.util.List;

public class TicketController {

    private final TicketManager ticketManager;

    public TicketController() {
        this.ticketManager = new TicketManager();
    }

    public List<Ticket> generateEntryTickets(Event event, Event.TicketOption option, int amount, String description) {
        return ticketManager.generateEntryTickets(event, option, amount, description);
    }

    public Ticket generateBenefitTicket(String ticketName, String valueText, String description,
                                        Event event, boolean validForAllEvents) {
        return ticketManager.generateBenefitTicket(ticketName, valueText, description, event, validForAllEvents);
    }

    public List<Ticket> getAllTickets() {
        return ticketManager.getAllTickets();
    }

    public boolean redeemTicket(String secureCode) {
        return ticketManager.redeemTicket(secureCode);
    }

    public boolean isValidEmail(String email) {
        return ticketManager.isValidEmail(email);
    }
}