package easv.be;

public class SoldTicketRecord {
    private final int soldTicketId;
    private final String ticketId;
    private final String eventName;
    private final String eventStartDateTime;
    private final String eventEndDateTime;
    private final String eventLocation;
    private final String eventLocationGuidance;
    private final String eventNotes;
    private final String customerName;
    private final String customerEmail;
    private final String ticketType;
    private final String ticketDescription;
    private final String price;
    private final boolean used;
    private final String publicCode;
    private final boolean specialTicket;
    private final boolean validForAllEvents;

    public SoldTicketRecord(int soldTicketId,
                            String ticketId,
                            String eventName,
                            String eventStartDateTime,
                            String eventEndDateTime,
                            String eventLocation,
                            String eventLocationGuidance,
                            String eventNotes,
                            String customerName,
                            String customerEmail,
                            String ticketType,
                            String ticketDescription,
                            String price,
                            boolean used,
                            String publicCode,
                            boolean specialTicket,
                            boolean validForAllEvents) {
        this.soldTicketId = soldTicketId;
        this.ticketId = ticketId;
        this.eventName = eventName;
        this.eventStartDateTime = eventStartDateTime;
        this.eventEndDateTime = eventEndDateTime;
        this.eventLocation = eventLocation;
        this.eventLocationGuidance = eventLocationGuidance;
        this.eventNotes = eventNotes;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.ticketType = ticketType;
        this.ticketDescription = ticketDescription;
        this.price = price;
        this.used = used;
        this.publicCode = publicCode;
        this.specialTicket = specialTicket;
        this.validForAllEvents = validForAllEvents;
    }

    public int getSoldTicketId() {
        return soldTicketId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventStartDateTime() {
        return eventStartDateTime;
    }

    public String getEventEndDateTime() {
        return eventEndDateTime;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public String getEventLocationGuidance() {
        return eventLocationGuidance;
    }

    public String getEventNotes() {
        return eventNotes;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getTicketType() {
        return ticketType;
    }

    public String getTicketDescription() {
        return ticketDescription;
    }

    public String getPrice() {
        return price;
    }

    public boolean isUsed() {
        return used;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public boolean isSpecialTicket() {
        return specialTicket;
    }

    public boolean isValidForAllEvents() {
        return validForAllEvents;
    }

    public boolean hasCustomer() {
        return customerName != null && !customerName.isBlank()
                || customerEmail != null && !customerEmail.isBlank();
    }
}
