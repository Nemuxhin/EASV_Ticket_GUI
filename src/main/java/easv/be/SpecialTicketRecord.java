package easv.be;

public class SpecialTicketRecord {
    private final int specialTicketId;
    private final String specialTicketName;
    private final String description;
    private final String eventName;
    private final String publicCode;
    private final String price;
    private final int quantity;
    private final boolean validForAllEvents;
    private final boolean active;

    public SpecialTicketRecord(int specialTicketId,
                               String specialTicketName,
                               String description,
                               String eventName,
                               String publicCode,
                               String price,
                               int quantity,
                               boolean validForAllEvents,
                               boolean active) {
        this.specialTicketId = specialTicketId;
        this.specialTicketName = specialTicketName;
        this.description = description;
        this.eventName = eventName;
        this.publicCode = publicCode;
        this.price = price;
        this.quantity = quantity;
        this.validForAllEvents = validForAllEvents;
        this.active = active;
    }

    public int getSpecialTicketId() {
        return specialTicketId;
    }

    public String getSpecialTicketName() {
        return specialTicketName;
    }

    public String getDescription() {
        return description;
    }

    public String getEventName() {
        return eventName;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public String getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isValidForAllEvents() {
        return validForAllEvents;
    }

    public boolean isActive() {
        return active;
    }
}
