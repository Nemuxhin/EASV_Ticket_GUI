package easv.be;

public class SoldTicketRecord {
    private final int soldTicketId;
    private final String eventName;
    private final String customerName;
    private final String customerEmail;
    private final String ticketType;
    private final String price;
    private final boolean used;
    private final String publicCode;

    public SoldTicketRecord(int soldTicketId,
                            String eventName,
                            String customerName,
                            String customerEmail,
                            String ticketType,
                            String price,
                            boolean used,
                            String publicCode) {
        this.soldTicketId = soldTicketId;
        this.eventName = eventName;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.ticketType = ticketType;
        this.price = price;
        this.used = used;
        this.publicCode = publicCode;
    }

    public int getSoldTicketId() {
        return soldTicketId;
    }

    public String getEventName() {
        return eventName;
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

    public String getPrice() {
        return price;
    }

    public boolean isUsed() {
        return used;
    }

    public String getPublicCode() {
        return publicCode;
    }
}
