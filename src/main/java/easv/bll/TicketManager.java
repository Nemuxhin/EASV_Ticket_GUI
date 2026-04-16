package easv.bll;

import easv.be.Customer;
import easv.be.Event;
import easv.be.SoldTicketRecord;
import easv.be.SpecialTicketRecord;
import easv.be.Ticket;
import easv.dal.CustomerDAO;
import easv.dal.EventDAO;
import easv.dal.SoldTicketDAO;
import easv.dal.SpecialTicketDAO;
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

        specialTicketDAO.createSpecialTicket(
                ticketType,
                ticketDescription,
                normalizedEventTitle,
                ticketGroupId,
                price,
                1,
                validForAllEvents
        );

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

        specialTicketDAO.createSpecialTicket(
                ticketType,
                ticketDescription,
                normalizedEventTitle,
                ticketGroupId,
                price,
                quantity,
                validForAllEvents
        );

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

    public List<Ticket> issueSpecialTicketDefinition(SpecialTicketRecord definition, Event event) {
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
                : event != null ? event.getTitle() : safeText(definition.getEventName());

        if (!definition.isValidForAllEvents() && (eventTitle == null || eventTitle.isBlank())) {
            throw new IllegalArgumentException("The event for this special ticket could not be found.");
        }

        String eventStartDateTime = event != null ? event.getStartDateTime() : "";
        String eventEndDateTime = event != null ? event.getEndDateTime() : "";
        String eventLocation = event != null ? event.getLocation() : "";
        String eventLocationGuidance = event != null ? event.getLocationGuidance() : "";
        String eventNotes = event != null
                ? mergeNotes(event.getNotes(), definition.getDescription())
                : safeDescription(definition.getDescription());

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
            if (ticket != null && ticket.isSpecialTicket()) {
                result.add(ticket);
            }
        }

        return result;
    }

    public List<SpecialTicketRecord> getSpecialTicketRecords() {
        return specialTicketDAO.getAllSpecialTickets();
    }

    public List<SoldTicketRecord> getSoldTickets() {
        return getRecentSoldTickets(10);
    }

    public List<SoldTicketRecord> getRecentSoldTickets(int limit) {
        return soldTicketDAO.getRecentSoldTickets(limit);
    }

    public List<SoldTicketRecord> searchSoldTickets(String query, int limit) {
        String needle = query == null ? "" : query.trim();

        if (needle.isBlank()) {
            return soldTicketDAO.getRecentSoldTickets(limit);
        }

        return soldTicketDAO.searchSoldTickets(needle, limit);
    }


    public boolean deactivateSpecialTicketGroup(String ticketGroupId) {
        List<Ticket> ticketsInGroup = ticketDAO.findByGroupId(ticketGroupId);

        boolean changed = false;
        for (Ticket ticket : ticketsInGroup) {
            if (ticket != null && ticket.isSpecialTicket() && ticket.isActive()) {
                ticket.setActive(false);
                changed = ticketDAO.updateTicket(ticket) || changed;
            }
        }

        boolean databaseChanged = specialTicketDAO.deactivateSpecialTicket(ticketGroupId);
        return changed || databaseChanged;
    }

    public LinkedHashMap<String, String> getTicketTypePricesForEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        LinkedHashMap<String, String> configured = ticketDAO.getTicketTypesForEvent(event);
        if (!configured.isEmpty()) {
            return configured;
        }

        LinkedHashMap<String, String> fallback = new LinkedHashMap<>();
        fallback.put("Standard", formatPrice(parsePriceValue(event.getPrice())));
        return fallback;
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
        String normalized = normalizeLookupValue(secureToken);
        if (normalized == null) {
            return null;
        }

        Ticket localTicket = ticketDAO.findByToken(normalized);
        if (localTicket != null) {
            return applySoldState(localTicket, soldTicketDAO.findByPublicCode(normalized));
        }

        SoldTicketRecord soldRecord = soldTicketDAO.findByPublicCode(normalized);
        return soldRecord == null ? null : buildTicketFromSoldRecord(soldRecord);
    }

    public Ticket findTicketByPublicCodeOrTicketId(String value) {
        String normalized = normalizeLookupValue(value);
        if (normalized == null) {
            return null;
        }

        Ticket byToken = findByToken(normalized);
        if (byToken != null) {
            return byToken;
        }

        Ticket localByTicketId = findLocalTicketByTicketId(normalized);
        if (localByTicketId != null) {
            return applySoldState(localByTicketId, soldTicketDAO.findByTicketId(normalized));
        }

        SoldTicketRecord soldRecord = soldTicketDAO.findByPublicCodeOrTicketId(normalized);
        return soldRecord == null ? null : buildTicketFromSoldRecord(soldRecord);
    }

    public SoldTicketRecord findSoldTicketByPublicCodeOrTicketId(String value) {
        return soldTicketDAO.findByPublicCodeOrTicketId(value);
    }

    public Ticket buildTicketFromSoldRecord(SoldTicketRecord soldRecord) {
        if (soldRecord == null) {
            return null;
        }

        Customer customer = null;
        if (soldRecord.hasCustomer()) {
            customer = new Customer(
                    "",
                    safeText(soldRecord.getCustomerName()),
                    safeText(soldRecord.getCustomerEmail())
            );
        }

        String publicCode = safeText(soldRecord.getPublicCode());
        byte[] qrImage = publicCode.isBlank() ? null : qrCodeGenerator.generateQrCode(publicCode);
        byte[] barcodeImage = publicCode.isBlank() ? null : barcodeGenerator.generateBarcode(publicCode);

        return new Ticket(
                safeText(soldRecord.getTicketId()),
                safeText(soldRecord.getTicketId()),
                publicCode,
                soldRecord.isUsed(),
                true,
                customer,
                safeText(soldRecord.getEventName()),
                safeText(soldRecord.getEventStartDateTime()),
                safeText(soldRecord.getEventEndDateTime()),
                safeText(soldRecord.getEventLocation()),
                safeText(soldRecord.getEventLocationGuidance()),
                safeText(soldRecord.getEventNotes()),
                safeText(soldRecord.getTicketType()),
                safeText(soldRecord.getTicketDescription()),
                safeText(soldRecord.getPrice()),
                soldRecord.isSpecialTicket(),
                soldRecord.isValidForAllEvents(),
                qrImage,
                barcodeImage
        );
    }

    public boolean isTicketValid(String lookupValue) {
        Ticket ticket = findTicketByPublicCodeOrTicketId(lookupValue);
        return ticket != null && ticket.isActive() && !ticket.isUsed();
    }

    public boolean isTicketValid(String lookupValue, String eventTitle) {
        Ticket ticket = findTicketByPublicCodeOrTicketId(lookupValue);

        if (ticket == null || !ticket.isActive() || ticket.isUsed()) {
            return false;
        }

        if (ticket.isValidForAllEvents()) {
            return true;
        }

        if (eventTitle == null || eventTitle.isBlank()) {
            return true;
        }

        return sameText(ticket.getEventTitle(), eventTitle);
    }

    public boolean markTicketAsUsed(String lookupValue) {
        return setUsageState(lookupValue, true);
    }

    public boolean setTicketUsedState(String ticketId, boolean used) {
        if (ticketId == null || ticketId.isBlank()) {
            return false;
        }

        Ticket localTicket = findLocalTicketByTicketId(ticketId.trim());
        if (localTicket != null) {
            if (!localTicket.isActive()) {
                return false;
            }

            localTicket.setUsed(used);
            ticketDAO.updateTicket(localTicket);
        }

        SoldTicketRecord soldRecord = soldTicketDAO.findByTicketId(ticketId.trim());
        if (soldRecord != null) {
            return soldTicketDAO.setUsedState(soldRecord.getPublicCode(), used);
        }

        return localTicket != null;
    }

    public boolean setSoldTicketUsedState(String publicCode, boolean used) {
        if (publicCode == null || publicCode.isBlank()) {
            return false;
        }

        boolean changed = soldTicketDAO.setUsedState(publicCode.trim(), used);
        if (!changed) {
            return false;
        }

        Ticket localTicket = ticketDAO.findByToken(publicCode.trim());
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

        int capacity = parseCapacity(event.getCapacity());
        if (capacity == Integer.MAX_VALUE) {
            return "Available";
        }

        int soldCount = countSoldTicketsForEvent(event);

        if (soldCount >= capacity) {
            return "Sold Out";
        }

        int remaining = capacity - soldCount;
        int fastSellingThreshold = Math.max(10, (int) Math.ceil(capacity * 0.20));

        return remaining <= fastSellingThreshold ? "Selling Fast" : "Available";
    }

    private boolean setUsageState(String lookupValue, boolean used) {
        String normalized = normalizeLookupValue(lookupValue);
        if (normalized == null) {
            return false;
        }

        Ticket ticket = findTicketByPublicCodeOrTicketId(normalized);
        if (ticket == null || !ticket.isActive()) {
            return false;
        }

        if (used && ticket.isUsed()) {
            return false;
        }

        SoldTicketRecord soldRecord = soldTicketDAO.findByPublicCodeOrTicketId(normalized);
        if (soldRecord == null || soldRecord.getPublicCode() == null || soldRecord.getPublicCode().isBlank()) {
            return false;
        }

        boolean changed = soldTicketDAO.setUsedState(soldRecord.getPublicCode(), used);
        if (!changed) {
            return false;
        }

        Ticket localByToken = ticketDAO.findByToken(soldRecord.getPublicCode());
        if (localByToken != null) {
            localByToken.setUsed(used);
            ticketDAO.updateTicket(localByToken);
        }

        Ticket localById = findLocalTicketByTicketId(safeText(soldRecord.getTicketId()));
        if (localById != null) {
            localById.setUsed(used);
            ticketDAO.updateTicket(localById);
        }

        return true;
    }

    private Ticket applySoldState(Ticket ticket, SoldTicketRecord soldRecord) {
        if (ticket == null || soldRecord == null) {
            return ticket;
        }

        if (ticket.isUsed() != soldRecord.isUsed()) {
            ticket.setUsed(soldRecord.isUsed());
            ticketDAO.updateTicket(ticket);
        }

        return ticket;
    }

    private Ticket findLocalTicketByTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            return null;
        }

        for (Ticket ticket : ticketDAO.getAllTickets()) {
            if (ticket != null && sameText(ticket.getTicketId(), ticketId)) {
                return ticket;
            }
        }

        return null;
    }

    private int countSoldTicketsForEvent(Event event) {
        backfillSoldTicketsFromLocalStore();

        int count = 0;

        for (SoldTicketRecord soldTicket : soldTicketDAO.getAllSoldTickets()) {
            if (soldTicket == null || soldTicket.isSpecialTicket()) {
                continue;
            }

            if (!soldTicket.hasCustomer()) {
                continue;
            }

            if (matchesEvent(soldTicket, event)) {
                count++;
            }
        }

        return count;
    }

    private boolean matchesEvent(SoldTicketRecord soldTicket, Event event) {
        if (soldTicket == null || event == null) {
            return false;
        }

        boolean sameTitle = sameText(soldTicket.getEventName(), event.getTitle());
        boolean sameLocation = sameText(soldTicket.getEventLocation(), event.getLocation());
        boolean sameStart = sameText(soldTicket.getEventStartDateTime(), event.getStartDateTime());

        if (!safeText(soldTicket.getEventLocation()).isBlank() && !safeText(soldTicket.getEventStartDateTime()).isBlank()) {
            return sameTitle && sameLocation && sameStart;
        }

        return sameTitle;
    }

    private void backfillSoldTicketsFromLocalStore() {
        for (Ticket ticket : ticketDAO.getAllTickets()) {
            if (ticket == null) {
                continue;
            }

            if (ticket.isSpecialTicket()) {
                if (!soldTicketDAO.existsByPublicCode(ticket.getSecureToken())) {
                    soldTicketDAO.saveSoldTicket(ticket);
                }
                continue;
            }

            if (!ticket.hasCustomer()) {
                continue;
            }

            if (!soldTicketDAO.existsByPublicCode(ticket.getSecureToken())) {
                soldTicketDAO.saveSoldTicket(ticket);
            }
        }
    }

    private void persistCustomerPurchase(Event event, Customer customer, String ticketType, int quantity) {
        if (event == null || customer == null || ticketType == null || ticketType.isBlank() || quantity < 1) {
            return;
        }

        Integer ticketTypeId = ticketDAO.findTicketTypeId(event, ticketType);
        if (ticketTypeId == null) {
            Customer persistedCustomer = customerDAO.save(customer);
            if (persistedCustomer != null) {
                customer.setCustomerId(persistedCustomer.getCustomerId());
            }
            return;
        }

        customerDAO.saveCustomerTickets(customer, ticketTypeId, quantity);
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

    private int parseCapacity(String capacityText) {
        if (capacityText == null || capacityText.isBlank()) {
            return Integer.MAX_VALUE;
        }

        String digitsOnly = capacityText.replaceAll("[^0-9]", "");
        if (digitsOnly.isBlank()) {
            return Integer.MAX_VALUE;
        }

        try {
            int parsed = Integer.parseInt(digitsOnly);
            return parsed > 0 ? parsed : Integer.MAX_VALUE;
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
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

    private boolean sameText(String first, String second) {
        return safeText(first).equalsIgnoreCase(safeText(second));
    }

    private String normalizeLookupValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
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

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
