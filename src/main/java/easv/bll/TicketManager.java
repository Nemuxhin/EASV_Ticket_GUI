package easv.bll;

import easv.be.Customer;
import easv.be.Event;
import easv.be.Ticket;
import easv.dal.TicketDAO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TicketManager {

    private final TicketDAO ticketDAO;
    private final TokenGenerator tokenGenerator;
    private final QrCodeGenerator qrCodeGenerator;
    private final BarcodeGenerator barcodeGenerator;

    public TicketManager() {
        this.ticketDAO = new TicketDAO();
        this.tokenGenerator = new TokenGenerator();
        this.qrCodeGenerator = new QrCodeGenerator();
        this.barcodeGenerator = new BarcodeGenerator();
    }

    public Ticket createEventTicket(Event event,
                                    Customer customer,
                                    String ticketType,
                                    String ticketDescription,
                                    String price,
                                    String endDateTime,
                                    String locationGuidance) {
        validateEventTicketInput(event, customer, ticketType, price);

        String ticketId = tokenGenerator.generateTicketId();

        return buildAndSaveTicket(
                ticketId,
                ticketId,
                tokenGenerator.generateSecureToken(),
                customer,
                event.getTitle(),
                event.getStartDateTime(),
                endDateTime,
                event.getLocation(),
                locationGuidance,
                event.getNotes(),
                ticketType,
                ticketDescription,
                price,
                false,
                false
        );
    }

    public List<Ticket> createEventTickets(Event event,
                                           Customer customer,
                                           String ticketType,
                                           String ticketDescription,
                                           String pricePerTicket,
                                           String endDateTime,
                                           String locationGuidance,
                                           int quantity) {
        validateEventTicketInput(event, customer, ticketType, pricePerTicket);
        validateQuantity(quantity);

        List<Ticket> createdTickets = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            String ticketId = tokenGenerator.generateTicketId();

            Ticket ticket = buildAndSaveTicket(
                    ticketId,
                    ticketId,
                    tokenGenerator.generateSecureToken(),
                    customer,
                    event.getTitle(),
                    event.getStartDateTime(),
                    endDateTime,
                    event.getLocation(),
                    locationGuidance,
                    event.getNotes(),
                    ticketType,
                    ticketDescription,
                    pricePerTicket,
                    false,
                    false
            );

            createdTickets.add(ticket);
        }

        return createdTickets;
    }

    public Ticket createSpecialTicket(String eventTitle,
                                      String eventStartDateTime,
                                      String eventEndDateTime,
                                      String eventLocation,
                                      String eventLocationGuidance,
                                      String eventNotes,
                                      String ticketType,
                                      String ticketDescription,
                                      String price,
                                      boolean validForAllEvents) {
        validateSpecialTicketInput(eventTitle, ticketType, price, validForAllEvents);

        String ticketId = tokenGenerator.generateTicketId();
        String ticketGroupId = tokenGenerator.generateSecureToken();

        return buildAndSaveTicket(
                ticketId,
                ticketGroupId,
                tokenGenerator.generateSecureToken(),
                null,
                normalizeEventTitle(eventTitle, validForAllEvents),
                eventStartDateTime,
                eventEndDateTime,
                eventLocation,
                eventLocationGuidance,
                eventNotes,
                ticketType,
                ticketDescription,
                price,
                true,
                validForAllEvents
        );
    }

    public List<Ticket> createSpecialTickets(String eventTitle,
                                             String eventStartDateTime,
                                             String eventEndDateTime,
                                             String eventLocation,
                                             String eventLocationGuidance,
                                             String eventNotes,
                                             String ticketType,
                                             String ticketDescription,
                                             String price,
                                             boolean validForAllEvents,
                                             int quantity) {
        validateSpecialTicketInput(eventTitle, ticketType, price, validForAllEvents);
        validateQuantity(quantity);

        List<Ticket> createdTickets = new ArrayList<>();
        String ticketGroupId = tokenGenerator.generateSecureToken();

        for (int i = 0; i < quantity; i++) {
            String ticketId = tokenGenerator.generateTicketId();

            Ticket ticket = buildAndSaveTicket(
                    ticketId,
                    ticketGroupId,
                    tokenGenerator.generateSecureToken(),
                    null,
                    normalizeEventTitle(eventTitle, validForAllEvents),
                    eventStartDateTime,
                    eventEndDateTime,
                    eventLocation,
                    eventLocationGuidance,
                    eventNotes,
                    ticketType,
                    ticketDescription,
                    price,
                    true,
                    validForAllEvents
            );

            createdTickets.add(ticket);
        }

        return createdTickets;
    }

    public List<Ticket> getAllTickets() {
        return ticketDAO.getAllTickets();
    }

    public List<Ticket> getSpecialTickets() {
        List<Ticket> result = new ArrayList<>();

        for (Ticket ticket : ticketDAO.getAllTickets()) {
            if (ticket.isSpecialTicket()) {
                result.add(ticket);
            }
        }

        return result;
    }

    public boolean deactivateSpecialTicketGroup(String ticketGroupId) {
        List<Ticket> ticketsInGroup = ticketDAO.findByGroupId(ticketGroupId);

        if (ticketsInGroup.isEmpty()) {
            return false;
        }

        boolean changed = false;

        for (Ticket ticket : ticketsInGroup) {
            if (ticket.isSpecialTicket() && ticket.isActive()) {
                ticket.setActive(false);
                boolean updated = ticketDAO.updateTicket(ticket);
                changed = changed || updated;
            }
        }

        return changed;
    }

    public LinkedHashMap<String, String> getTicketTypePricesForEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        LinkedHashMap<String, String> ticketTypePrices = new LinkedHashMap<>();

        double basePrice = parsePriceValue(event.getPrice());

        ticketTypePrices.put("Standard", formatPrice(basePrice));
        ticketTypePrices.put("VIP", formatPrice(basePrice * 1.5));
        ticketTypePrices.put("Student", formatPrice(basePrice * 0.7));

        LinkedHashMap<String, List<Ticket>> groupedSpecialTickets = groupSpecialTicketsByBatch();

        for (List<Ticket> group : groupedSpecialTickets.values()) {
            if (group.isEmpty()) {
                continue;
            }

            Ticket representative = group.get(0);

            if (!representative.isActive()) {
                continue;
            }

            if (!representative.isValidForAllEvents() && !sameEventTitle(representative.getEventTitle(), event.getTitle())) {
                continue;
            }

            int remainingCount = countRemainingTickets(group);
            if (remainingCount <= 0) {
                continue;
            }

            String typeName = safeTicketTypeName(representative.getTicketType());
            if (!ticketTypePrices.containsKey(typeName)) {
                ticketTypePrices.put(typeName, normalizePrice(representative.getPrice()));
            }
        }

        return ticketTypePrices;
    }

    public Ticket findByToken(String secureToken) {
        return ticketDAO.findByToken(secureToken);
    }

    public boolean isTicketValid(String secureToken) {
        Ticket ticket = ticketDAO.findByToken(secureToken);
        return ticket != null && ticket.isActive() && !ticket.isUsed();
    }

    public boolean isTicketValid(String secureToken, String eventTitle) {
        Ticket ticket = ticketDAO.findByToken(secureToken);

        if (ticket == null || !ticket.isActive() || ticket.isUsed()) {
            return false;
        }

        if (ticket.isValidForAllEvents()) {
            return true;
        }

        if (eventTitle == null || eventTitle.isBlank()) {
            return true;
        }

        return sameEventTitle(ticket.getEventTitle(), eventTitle);
    }

    public boolean markTicketAsUsed(String secureToken) {
        Ticket ticket = ticketDAO.findByToken(secureToken);

        if (ticket == null || !ticket.isActive() || ticket.isUsed()) {
            return false;
        }

        ticket.setUsed(true);
        return ticketDAO.updateTicket(ticket);
    }

    private Ticket buildAndSaveTicket(String ticketId,
                                      String ticketGroupId,
                                      String secureToken,
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
                                      boolean validForAllEvents) {

        byte[] qrImage = qrCodeGenerator.generateQrCode(secureToken);
        byte[] barcodeImage = barcodeGenerator.generateBarcode(secureToken);

        Ticket ticket = new Ticket(
                ticketId,
                ticketGroupId,
                secureToken,
                false,
                true,
                customer,
                eventTitle,
                eventStartDateTime,
                eventEndDateTime,
                eventLocation,
                eventLocationGuidance,
                eventNotes,
                ticketType,
                ticketDescription,
                price,
                specialTicket,
                validForAllEvents,
                qrImage,
                barcodeImage
        );

        ticketDAO.addTicket(ticket);
        return ticket;
    }

    private LinkedHashMap<String, List<Ticket>> groupSpecialTicketsByBatch() {
        LinkedHashMap<String, List<Ticket>> grouped = new LinkedHashMap<>();

        for (Ticket ticket : ticketDAO.getAllTickets()) {
            if (!ticket.isSpecialTicket()) {
                continue;
            }

            String key = ticket.getTicketGroupId();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(ticket);
        }

        return grouped;
    }

    private int countRemainingTickets(List<Ticket> tickets) {
        int count = 0;

        for (Ticket ticket : tickets) {
            if (ticket.isActive() && !ticket.isUsed()) {
                count++;
            }
        }

        return count;
    }

    private void validateEventTicketInput(Event event,
                                          Customer customer,
                                          String ticketType,
                                          String price) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        if (ticketType == null || ticketType.isBlank()) {
            throw new IllegalArgumentException("Ticket type cannot be blank.");
        }

        if (price == null || price.isBlank()) {
            throw new IllegalArgumentException("Price cannot be blank.");
        }
    }

    private void validateSpecialTicketInput(String eventTitle,
                                            String ticketType,
                                            String price,
                                            boolean validForAllEvents) {
        if (!validForAllEvents && (eventTitle == null || eventTitle.isBlank())) {
            throw new IllegalArgumentException("Event title is required unless the ticket is valid for all events.");
        }

        if (ticketType == null || ticketType.isBlank()) {
            throw new IllegalArgumentException("Ticket type cannot be blank.");
        }

        if (price == null || price.isBlank()) {
            throw new IllegalArgumentException("Price cannot be blank.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
    }

    private String normalizeEventTitle(String eventTitle, boolean validForAllEvents) {
        if (validForAllEvents && (eventTitle == null || eventTitle.isBlank())) {
            return "All Events";
        }
        return eventTitle;
    }

    private String safeTicketTypeName(String ticketType) {
        if (ticketType == null || ticketType.isBlank()) {
            return "Special Ticket";
        }
        return ticketType.trim();
    }

    private boolean sameEventTitle(String first, String second) {
        if (first == null || second == null) {
            return false;
        }
        return first.trim().equalsIgnoreCase(second.trim());
    }

    private String normalizePrice(String rawPrice) {
        return formatPrice(parsePriceValue(rawPrice));
    }

    private double parsePriceValue(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank() || "Free".equalsIgnoreCase(rawPrice.trim())) {
            return 0;
        }

        String cleaned = rawPrice
                .replace("DKK", "")
                .replace("dkk", "")
                .replace(",", ".")
                .trim();

        if (cleaned.isBlank()) {
            return 0;
        }

        return Double.parseDouble(cleaned);
    }

    private String formatPrice(double amount) {
        if (amount == 0) {
            return "Free";
        }
        if (amount == Math.floor(amount)) {
            return String.format(Locale.ENGLISH, "%.0f DKK", amount);
        }
        return String.format(Locale.ENGLISH, "%.2f DKK", amount);
    }
}