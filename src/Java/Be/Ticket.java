package Java.Be;

public class Ticket {
    private String ticketId;
    private String ticketType;
    private String eventTitle;

    public Ticket(String ticketId, String ticketType, String eventTitle) {
        this.ticketId = ticketId;
        this.ticketType = ticketType;
        this.eventTitle = eventTitle;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getTicketType() {
        return ticketType;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
}