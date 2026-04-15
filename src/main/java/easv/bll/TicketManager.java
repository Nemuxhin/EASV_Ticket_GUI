package easv.bll;

import easv.be.Customer;
import easv.be.Event;
import easv.be.SoldTicketRecord;
import easv.be.SpecialTicketRecord;
import easv.be.Ticket;
import easv.dal.CustomerDAO;
import easv.dal.EventDAO;
import easv.dal.SpecialTicketDAO;
import easv.dal.SoldTicketDAO;
import easv.dal.TicketDAO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class TicketManager {

    private final TicketDAO ticketDAO;
    private final CustomerDAO customerDAO;
    private final EventDAO eventDAO;
    private final SpecialTicketDAO specialTicketDAO;
    private final SoldTicketDAO soldTicketDAO;
    private final TokenGenerator tokenGenerator;
    private final QrCodeGenerator qrCodeGenerator;
    private final BarcodeGenerator barcodeGenerator;

    public TicketManager() {
        this.ticketDAO = new TicketDAO();
        this.customerDAO = new CustomerDAO();
        this.eventDAO = new EventDAO();
        this.specialTicketDAO = new SpecialTicketDAO();
        this.soldTicketDAO = new SoldTicketDAO();
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
                price,
                false,
                false
        );

        soldTicketDAO.saveSoldTicket(ticket);
        syncEventStatus(event);
        return ticket;
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

        persistCustomerPurchase(event, customer, ticketType, quantity);

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
            soldTicketDAO.saveSoldTicket(ticket);
        }

        syncEventStatus(event);
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
        String normalizedEventTitle = normalizeEventTitle(eventTitle, validForAllEvents);

        specialTicketDAO.createSpecialTicket(ticketType, ticketDescription, normalizedEventTitle, ticketGroupId, price, 1, validForAllEvents);

        Ticket ticket = buildAndSaveTicket(
                ticketId,
                ticketGroupId,
                tokenGenerator.generateSecureToken(),
                null,
                normalizedEventTitle,
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

        soldTicketDAO.saveSoldTicket(ticket);
        return ticket;
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
        String normalizedEventTitle = normalizeEventTitle(eventTitle, validForAllEvents);

        specialTicketDAO.createSpecialTicket(ticketType, ticketDescription, normalizedEventTitle, ticketGroupId, price, quantity, validForAllEvents);

        for (int i = 0; i < quantity; i++) {
            String ticketId = tokenGenerator.generateTicketId();

            Ticket ticket = buildAndSaveTicket(
                ticketId,
                ticketGroupId,
                tokenGenerator.generateSecureToken(),
                null,
                normalizedEventTitle,
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
            soldTicketDAO.saveSoldTicket(ticket);
        }

        return createdTickets;
    }

    public void createSpecialTicketDefinition(String eventTitle,
                                              String ticketType,
                                              String ticketDescription,
                                              String price,
                                              int quantity,
                                              boolean validForAllEvents) {
        validateSpecialTicketInput(eventTitle, ticketType, price, validForAllEvents);
        validateQuantity(quantity);

        String publicCode = tokenGenerator.generateSecureToken();
        String normalizedEventTitle = normalizeEventTitle(eventTitle, validForAllEvents);
        specialTicketDAO.createSpecialTicket(
                ticketType,
                ticketDescription,
                normalizedEventTitle,
                publicCode,
                price,
                quantity,
                validForAllEvents
        );
    }

    public List<Ticket> issueSpecialTicketDefinition(SpecialTicketRecord definition,
                                                     Event event) {
        if (definition == null) {
            throw new IllegalArgumentException("Special ticket definition cannot be null.");
        }

        if (!definition.isActive()) {
            throw new IllegalArgumentException("The selected special ticket is not active.");
        }

        int quantity = definition.getQuantity();
        validateQuantity(quantity);

        String eventTitle = definition.isValidForAllEvents()
                ? normalizeEventTitle(null, true)
                : event != null ? event.getTitle() : safeEventTitle(definition.getEventName());

        if (!definition.isValidForAllEvents() && (eventTitle == null || eventTitle.isBlank())) {
            throw new IllegalArgumentException("The event for this special ticket could not be found.");
        }

        String eventStartDateTime = event != null ? event.getStartDateTime() : "";
        String eventEndDateTime = event != null ? event.getEndDateTime() : "";
        String eventLocation = event != null ? event.getLocation() : "";
        String eventLocationGuidance = event != null ? event.getLocationGuidance() : "";
        String eventNotes = event != null ? mergeNotes(event.getNotes(), definition.getDescription()) : safeDescription(definition.getDescription());

        List<Ticket> createdTickets = new ArrayList<>();
        String ticketGroupId = definition.getPublicCode();

        for (int i = 0; i < quantity; i++) {
            String ticketId = tokenGenerator.generateTicketId();

            Ticket ticket = buildAndSaveTicket(
                    ticketId,
                    ticketGroupId,
                    tokenGenerator.generateSecureToken(),
                    null,
                    eventTitle,
                    eventStartDateTime,
                    eventEndDateTime,
                    eventLocation,
                    eventLocationGuidance,
                    eventNotes,
                    definition.getSpecialTicketName(),
                    safeDescription(definition.getDescription()),
                    definition.getPrice(),
                    true,
                    definition.isValidForAllEvents()
            );

            createdTickets.add(ticket);
            soldTicketDAO.saveSoldTicket(ticket);
        }

        specialTicketDAO.deactivateSpecialTicket(definition.getPublicCode());
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

    public List<SpecialTicketRecord> getSpecialTicketRecords() {
        return specialTicketDAO.getAllSpecialTickets();
    }

    public List<SoldTicketRecord> getSoldTickets() {
        return soldTicketDAO.getAllSoldTickets();
    }

    public boolean deactivateSpecialTicketGroup(String ticketGroupId) {
        List<Ticket> ticketsInGroup = ticketDAO.findByGroupId(ticketGroupId);

        if (ticketsInGroup.isEmpty()) {
            return specialTicketDAO.deactivateSpecialTicket(ticketGroupId);
        }

        boolean changed = false;

        for (Ticket ticket : ticketsInGroup) {
            if (ticket.isSpecialTicket() && ticket.isActive()) {
                ticket.setActive(false);
                boolean updated = ticketDAO.updateTicket(ticket);
                changed = changed || updated;
            }
        }

        boolean databaseChanged = specialTicketDAO.deactivateSpecialTicket(ticketGroupId);
        return changed || databaseChanged;
    }

    public LinkedHashMap<String, String> getTicketTypePricesForEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        return getBaseTicketTypes(event);
    }

    public LinkedHashMap<String, String> getConfiguredTicketTypesForEvent(Event event) {
        return ticketDAO.getTicketTypesForEvent(event);
    }

    public void setConfiguredTicketTypesForEvent(Event event, LinkedHashMap<String, String> ticketTypes) {
        ticketDAO.setTicketTypesForEvent(event, ticketTypes);
    }

    public void moveConfiguredTicketTypes(Event oldEvent, Event updatedEvent) {
        ticketDAO.moveTicketTypesToUpdatedEvent(oldEvent, updatedEvent);
    }

    public void removeConfiguredTicketTypes(Event event) {
        ticketDAO.removeTicketTypesForEvent(event);
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

    public boolean setTicketUsedState(String ticketId, boolean used) {
        if (ticketId == null || ticketId.isBlank()) {
            return false;
        }

        for (Ticket ticket : ticketDAO.getAllTickets()) {
            if (!ticketId.equals(ticket.getTicketId())) {
                continue;
            }

            if (!ticket.isActive()) {
                return false;
            }

            ticket.setUsed(used);
            return ticketDAO.updateTicket(ticket);
        }

        return false;
    }

    public boolean setSoldTicketUsedState(String publicCode, boolean used) {
        if (publicCode == null || publicCode.isBlank()) {
            return false;
        }

        boolean changed = soldTicketDAO.setUsedState(publicCode, used);
        if (!changed) {
            return false;
        }

        Ticket localTicket = ticketDAO.findByToken(publicCode);
        if (localTicket != null) {
            localTicket.setUsed(used);
            ticketDAO.updateTicket(localTicket);
        }

        return true;
    }

    public String getEventStatus(Event event) {
        if (event == null) {
            return "Available";
        }

        backfillSoldTicketsFromLocalStore();

        String capacityText = event.getCapacity();
        if (capacityText == null || capacityText.isBlank()) {
            return "Available";
        }

        int capacity = Integer.parseInt(capacityText.trim());
        if (capacity <= 0) {
            return "Sold Out";
        }

        int soldTickets = 0;
        for (SoldTicketRecord soldTicket : soldTicketDAO.getAllSoldTickets()) {
            if (!sameEventTitle(soldTicket.getEventName(), event.getTitle())) {
                continue;
            }

            if ((soldTicket.getCustomerName() == null || soldTicket.getCustomerName().isBlank())
                    && (soldTicket.getCustomerEmail() == null || soldTicket.getCustomerEmail().isBlank())) {
                continue;
            }

            soldTickets++;
        }

        if (soldTickets >= capacity) {
            return "Sold Out";
        }

        int remainingTickets = capacity - soldTickets;
        int fastSellingThreshold = Math.max(10, (int) Math.ceil(capacity * 0.2));
        return remainingTickets <= fastSellingThreshold ? "Fast Selling" : "Available";
    }

    private void backfillSoldTicketsFromLocalStore() {
        for (Ticket ticket : ticketDAO.getAllTickets()) {
            if (ticket == null || ticket.isSpecialTicket() || !ticket.hasCustomer()) {
                continue;
            }

            if (soldTicketDAO.existsByPublicCode(ticket.getSecureToken())) {
                continue;
            }

            soldTicketDAO.saveSoldTicket(ticket);
        }
    }

    private void persistCustomerPurchase(Event event, Customer customer, String ticketType, int quantity) {
        if (event == null || customer == null || ticketType == null || ticketType.isBlank() || quantity < 1) {
            return;
        }

        Integer ticketId = ticketDAO.findTicketTypeId(event, ticketType);
        if (ticketId == null) {
            Customer persistedCustomer = customerDAO.save(customer);
            if (persistedCustomer != null) {
                customer.setCustomerId(persistedCustomer.getCustomerId());
            }
            return;
        }

        customerDAO.saveCustomerTickets(customer, ticketId, quantity);
    }

    private void syncEventStatus(Event event) {
        if (event == null) {
            return;
        }

        String status = getEventStatus(event);
        event.setStatus(status);
        eventDAO.updateEventStatus(event, status);
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

    private LinkedHashMap<String, String> getBaseTicketTypes(Event event) {
        LinkedHashMap<String, String> configured = ticketDAO.getTicketTypesForEvent(event);
        if (!configured.isEmpty()) {
            return configured;
        }

        LinkedHashMap<String, String> fallback = new LinkedHashMap<>();
        double basePrice = parsePriceValue(event.getPrice());

        fallback.put("Standard", formatPrice(basePrice));
        return fallback;
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

    private String safeEventTitle(String eventTitle) {
        return eventTitle == null ? "" : eventTitle.trim();
    }

    private String safeDescription(String description) {
        return description == null || description.isBlank() ? "Special ticket" : description.trim();
    }

    private String mergeNotes(String eventNotes, String specialDescription) {
        if (eventNotes == null || eventNotes.isBlank()) {
            return safeDescription(specialDescription);
        }

        if (specialDescription == null || specialDescription.isBlank()) {
            return eventNotes.trim();
        }

        return eventNotes.trim() + " " + specialDescription.trim();
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
