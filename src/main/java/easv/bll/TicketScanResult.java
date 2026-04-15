package easv.bll;

import easv.be.Ticket;

public record TicketScanResult(boolean success, String title, String message, Ticket ticket) {

    public static TicketScanResult ok(Ticket ticket, String message) {
        return new TicketScanResult(true, "Ticket Approved", message, ticket);
    }

    public static TicketScanResult fail(String title, String message) {
        return new TicketScanResult(false, title, message, null);
    }
}
