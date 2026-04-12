package easv.be;

public class TicketPurchase {
    private final Event event;
    private final String customerName;
    private final String customerEmail;
    private final String ticketType;
    private final int quantity;
    private final double totalPrice;

    public TicketPurchase(Event event, String customerName, String customerEmail, String ticketType, int quantity, double totalPrice) {
        this.event = event;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.ticketType = ticketType;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public Event getEvent() { return event; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getTicketType() { return ticketType; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
}
