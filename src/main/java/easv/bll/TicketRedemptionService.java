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
                    "No QR token, public code, or ticket ID was provided."
            );
        }

        Ticket ticket = ticketController.findTicketByPublicCodeOrTicketId(input);
        if (ticket == null) {
            return TicketScanResult.fail(
                    "Ticket Not Found",
                    "No ticket matches the scanned code or entered ticket ID."
            );
        }

        if (!ticket.isActive()) {
            return TicketScanResult.fail(
                    "Ticket Inactive",
                    "This ticket is no longer active.",
                    ticket
            );
        }

        if (ticket.isUsed()) {
            return TicketScanResult.fail(
                    "Already Used",
                    "This ticket has already been scanned and cannot be used again.",
                    ticket
            );
        }

        if (selectedEvent != null && !ticket.matchesEvent(selectedEvent.getTitle())) {
            return TicketScanResult.fail(
                    "Wrong Event",
                    "This ticket is not valid for the selected event.",
                    ticket
            );
        }

        boolean updated = ticketController.markTicketAsUsed(input);
        if (!updated) {
            Ticket latestTicket = ticketController.findTicketByPublicCodeOrTicketId(input);
            return TicketScanResult.fail(
                    "Scan Failed",
                    "The ticket was found, but it could not be marked as used.",
                    latestTicket != null ? latestTicket : ticket
            );
        }

        Ticket updatedTicket = ticketController.findTicketByPublicCodeOrTicketId(input);
        return TicketScanResult.ok(
                updatedTicket != null ? updatedTicket : ticket,
                "Ticket accepted and marked as used."
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
