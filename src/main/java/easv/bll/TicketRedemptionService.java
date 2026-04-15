package easv.bll;

import easv.be.Event;
import easv.be.Ticket;
import easv.be.User;
import easv.controller.TicketController;

public class TicketRedemptionService {

    private final TicketController ticketController;

    public TicketRedemptionService() {
        this.ticketController = new TicketController();
    }

    public TicketScanResult redeem(User currentUser, Event selectedEvent, String secureToken) {
        if (currentUser == null || !"Event Coordinator".equalsIgnoreCase(currentUser.getRole())) {
            return TicketScanResult.fail(
                    "Access Denied",
                    "Only event coordinators are allowed to scan and redeem tickets."
            );
        }

        if (secureToken == null || secureToken.isBlank()) {
            return TicketScanResult.fail(
                    "Scan Failed",
                    "No QR token was found."
            );
        }

        Ticket ticket = ticketController.findByToken(secureToken.trim());

        if (ticket == null) {
            return TicketScanResult.fail(
                    "Ticket Not Found",
                    "No ticket matches the scanned code."
            );
        }

        if (!ticket.isActive()) {
            return TicketScanResult.fail(
                    "Ticket Inactive",
                    "This ticket is no longer active."
            );
        }

        if (ticket.isUsed()) {
            return TicketScanResult.fail(
                    "Already Used",
                    "This ticket has already been scanned and cannot be used again."
            );
        }

        if (selectedEvent != null && !ticket.matchesEvent(selectedEvent.getTitle())) {
            return TicketScanResult.fail(
                    "Wrong Event",
                    "This ticket is not valid for the selected event."
            );
        }

        boolean updated = ticketController.markTicketAsUsed(secureToken.trim());

        if (!updated) {
            return TicketScanResult.fail(
                    "Scan Failed",
                    "The ticket was found, but it could not be marked as used."
            );
        }

        Ticket updatedTicket = ticketController.findByToken(secureToken.trim());
        return TicketScanResult.ok(
                updatedTicket != null ? updatedTicket : ticket,
                "Ticket accepted and marked as used."
        );
    }
}
