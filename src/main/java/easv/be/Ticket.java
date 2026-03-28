package easv.be;

public class Ticket {
    private String ticketId;
    private String secureToken;
    private boolean used;

    private Customer customer;

    private String eventTitle;
    private String eventStartDateTime;
    private String eventEndDateTime;
    private String eventLocation;
    private String eventLocationGuidance;
    private String eventNotes;

    private String ticketType;
    private String ticketDescription;
    private String price;

    private boolean specialTicket;
    private boolean validForAllEvents;

    private byte[] qrImage;
    private byte[] barcodeImage;

    public Ticket(String ticketId,
                  String secureToken,
                  boolean used,
                  Customer customer,
                  String eventTitle,
                  String eventStartDateTime,
                  String eventEndDateTime,
                  String eventLocation,
                  String eventLocationGuidance,
                  String eventNotes,
                  String ticketType,
                  String ticketDescription,
                  String price,
                  boolean specialTicket,
                  boolean validForAllEvents,
                  byte[] qrImage,
                  byte[] barcodeImage) {
        this.ticketId = ticketId;
        this.secureToken = secureToken;
        this.used = used;
        this.customer = customer;
        this.eventTitle = eventTitle;
        this.eventStartDateTime = eventStartDateTime;
        this.eventEndDateTime = eventEndDateTime;
        this.eventLocation = eventLocation;
        this.eventLocationGuidance = eventLocationGuidance;
        this.eventNotes = eventNotes;
        this.ticketType = ticketType;
        this.ticketDescription = ticketDescription;
        this.price = price;
        this.specialTicket = specialTicket;
        this.validForAllEvents = validForAllEvents;
        this.qrImage = qrImage;
        this.barcodeImage = barcodeImage;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getSecureToken() {
        return secureToken;
    }

    public boolean isUsed() {
        return used;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getEventTitle() {
        return eventTitle;
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

    public String getTicketType() {
        return ticketType;
    }

    public String getTicketDescription() {
        return ticketDescription;
    }

    public String getPrice() {
        return price;
    }

    public boolean isSpecialTicket() {
        return specialTicket;
    }

    public boolean isValidForAllEvents() {
        return validForAllEvents;
    }

    public byte[] getQrImage() {
        return qrImage;
    }

    public byte[] getBarcodeImage() {
        return barcodeImage;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}