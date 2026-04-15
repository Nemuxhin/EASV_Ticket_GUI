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

    public TicketScanResult redeem(User currentUser, Event selectedEvent, String scannedValue) {
        if (currentUser == null || !"Event Coordinator".equalsIgnoreCase(currentUser.getRole())) {
            return TicketScanResult.fail(
                    "Access Denied",
                    "Only event coordinators are allowed to scan and redeem tickets."
            );
        }

        String input = normalize(scannedValue);
        if (input == null) {
            return TicketScanResult.fail(
                    "Scan Failed",
                    "No QR token or ticket ID was provided."
            );
        }

        Ticket ticket = resolveTicket(input);
        if (ticket == null) {
            return TicketScanResult.fail(
                    "Ticket Not Found",
                    "No ticket matches the scanned code or entered ticket ID."
            );
        }

        if (!ticket.isActive()) {
            return new TicketScanResult(
                    false,
                    "Ticket Inactive",
                    "This ticket is no longer active.",
                    ticket
            );
        }

        if (ticket.isUsed()) {
            return new TicketScanResult(
                    false,
                    "Already Used",
                    "This ticket has already been scanned and cannot be used again.",
                    ticket
            );
        }

        if (selectedEvent != null && !ticket.matchesEvent(selectedEvent.getTitle())) {
            return new TicketScanResult(
                    false,
                    "Wrong Event",
                    "This ticket is not valid for the selected event.",
                    ticket
            );
        }

        String secureToken = normalize(ticket.getSecureToken());
        if (secureToken == null) {
            return new TicketScanResult(
                    false,
                    "Scan Failed",
                    "This ticket does not contain a valid redeemable token.",
                    ticket
            );
        }

        boolean updated = ticketController.markTicketAsUsed(secureToken);
        if (!updated) {
            Ticket latestTicket = resolveTicket(input);
            return new TicketScanResult(
                    false,
                    "Scan Failed",
                    "The ticket was found, but it could not be marked as used.",
                    latestTicket != null ? latestTicket : ticket
            );
        }

        Ticket updatedTicket = ticketController.findByToken(secureToken);
        return TicketScanResult.ok(
                updatedTicket != null ? updatedTicket : ticket,
                "Ticket accepted and marked as used."
        );
    }

    private Ticket resolveTicket(String input) {
        Ticket byToken = ticketController.findByToken(input);
        if (byToken != null) {
            return byToken;
        }

        for (Ticket ticket : ticketController.getAllTickets()) {
            String ticketId = normalize(ticket.getTicketId());
            if (ticketId != null && ticketId.equalsIgnoreCase(input)) {
                return ticket;
            }
        }

        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
