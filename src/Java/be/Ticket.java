package Java.be;

public class Ticket {

    public static final String KIND_ENTRY = "ENTRY";
    public static final String KIND_BENEFIT = "BENEFIT";

    private String ticketId;
    private String secureCode;

    private byte[] qrCodeImage;
    private byte[] barcode1DImage;

    private Event event; // nullable if valid for all events
    private boolean validForAllEvents;

    private String ticketKind;      // ENTRY or BENEFIT
    private String ticketName;      // VIP, Food Included, One Free Beer, etc.
    private String valueText;       // 250 DKK, Free, 50% off one drink, etc.
    private String description;

    private boolean used;

    public Ticket(String ticketId, String secureCode, byte[] qrCodeImage, byte[] barcode1DImage,
                  Event event, boolean validForAllEvents,
                  String ticketKind, String ticketName, String valueText, String description,
                  boolean used) {
        this.ticketId = ticketId;
        this.secureCode = secureCode;
        this.qrCodeImage = qrCodeImage;
        this.barcode1DImage = barcode1DImage;
        this.event = event;
        this.validForAllEvents = validForAllEvents;
        this.ticketKind = ticketKind;
        this.ticketName = ticketName;
        this.valueText = valueText;
        this.description = description;
        this.used = used;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getSecureCode() {
        return secureCode;
    }

    public byte[] getQrCodeImage() {
        return qrCodeImage;
    }

    public byte[] getBarcode1DImage() {
        return barcode1DImage;
    }

    public Event getEvent() {
        return event;
    }

    public boolean isValidForAllEvents() {
        return validForAllEvents;
    }

    public String getTicketKind() {
        return ticketKind;
    }

    public String getTicketName() {
        return ticketName;
    }

    public String getValueText() {
        return valueText;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUsed() {
        return used;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public void setQrCodeImage(byte[] qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }

    public void setBarcode1DImage(byte[] barcode1DImage) {
        this.barcode1DImage = barcode1DImage;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setValidForAllEvents(boolean validForAllEvents) {
        this.validForAllEvents = validForAllEvents;
    }

    public void setTicketKind(String ticketKind) {
        this.ticketKind = ticketKind;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}