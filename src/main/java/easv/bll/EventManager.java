package easv.bll;

import easv.be.Event;
import easv.be.TicketPurchase;
import easv.be.User;
import easv.dal.EventDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class EventManager {
    private static final DateTimeFormatter EVENT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter EVENT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final EventDAO eventDAO = new EventDAO();

    public List<Event> getEvents() {
        return eventDAO.getAllEvents();
    }

    public List<Event> getEventsForUser(User user) {
        if (user == null) {
            return getEvents();
        }

        if ("Admin".equalsIgnoreCase(user.getRole())) {
            return getEvents();
        }

        if ("Event Coordinator".equalsIgnoreCase(user.getRole())) {
            return eventDAO.getEventsForCoordinator(user.getUsername());
        }

        return List.of();
    }

    public List<Event> getArchivedEvents() {
        return eventDAO.getArchivedEvents();
    }

    public void createEvent(Event event) {
        eventDAO.addEvent(event);
    }

    public void addEvent(Event event) {
        eventDAO.addEvent(event);
    }

    public boolean updateEvent(Event currentEvent, Event updatedEvent) {
        return eventDAO.updateEvent(currentEvent, updatedEvent);
    }

    public void deleteEvent(Event event) {
        eventDAO.deleteEvent(event);
    }

    public void restoreEvent(Event event) {
        eventDAO.restoreEvent(event);
    }

    public void assignCoordinator(Event event, String coordinatorName) {
        eventDAO.assignCoordinator(event, coordinatorName);
    }

    public void setCoordinators(Event event, String[] coordinators) {
        eventDAO.setCoordinators(event, coordinators);
    }

    public void updateEventStatus(Event event, String status) {
        eventDAO.updateEventStatus(event, status);
    }

    public String validateEvent(String title, String date, String time, String venue, String capacity, String price) {
        if (title == null || title.trim().isEmpty()) return "Please enter an event title.";
        if (date == null || date.trim().isEmpty()) return "Please enter an event date.";
        if (time == null || time.trim().isEmpty()) return "Please enter an event time.";

        try {
            LocalDate.parse(date.trim(), EVENT_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return "Please enter a valid event date in the format dd/MM/yyyy.";
        }

        try {
            LocalTime.parse(time.trim(), EVENT_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return "Please enter a valid event time in the format HH:mm.";
        }

        if (venue == null || venue.trim().isEmpty()) return "Please enter a venue.";
        if (capacity == null || capacity.trim().isEmpty()) return "Please enter the event capacity.";
        if (price == null || price.trim().isEmpty()) return "Please enter the ticket price.";

        try {
            if (Integer.parseInt(capacity.trim()) < 1) return "Capacity must be at least 1.";
        } catch (NumberFormatException e) {
            return "Capacity must be a whole number.";
        }

        try {
            if (Double.parseDouble(price.trim().replace(",", ".")) < 0) return "Price cannot be negative.";
        } catch (NumberFormatException e) {
            return "Price must be a number.";
        }

        return null;
    }

    public Event createEvent(String title, String date, String time, String venue, String notes, String price) {
        Event event = new Event(
                title.trim(),
                date.trim() + " at " + time.trim(),
                "",
                venue.trim(),
                "",
                notes == null ? "" : notes.trim(),
                formatPrice(price),
                "Available",
                new String[0]
        );
        eventDAO.addEvent(event);
        return event;
    }

    public boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty()
                && trimmed.contains("@")
                && trimmed.contains(".")
                && trimmed.indexOf('@') > 0
                && trimmed.indexOf('.') > trimmed.indexOf('@') + 1;
    }

    public String validatePurchase(String customerName, String customerEmail, String ticketType, int quantity) {
        if (customerName == null || customerName.trim().isEmpty()) return "Please enter the customer's full name.";
        if (customerEmail == null || customerEmail.trim().isEmpty()) return "Please enter the customer's email address.";
        if (!isValidEmail(customerEmail)) return "Please enter a valid email address.";
        if (ticketType == null || ticketType.trim().isEmpty()) return "Please select a ticket type.";
        if (quantity < 1) return "Quantity must be at least 1.";
        return null;
    }

    public double calculateTotalPrice(Event event, String ticketType, int quantity) {
        double basePrice = parseBasePrice(event.getPrice());
        double ticketPrice = switch (ticketType.toUpperCase(Locale.ENGLISH)) {
            case "VIP" -> basePrice * 1.5;
            case "STUDENT" -> basePrice * 0.7;
            default -> basePrice;
        };
        return ticketPrice * quantity;
    }

    public TicketPurchase createTicketPurchase(Event event, String customerName, String customerEmail, String ticketType, int quantity) {
        return new TicketPurchase(
                event,
                customerName.trim(),
                customerEmail.trim(),
                ticketType.toUpperCase(Locale.ENGLISH),
                quantity,
                calculateTotalPrice(event, ticketType, quantity)
        );
    }

    private double parseBasePrice(String priceText) {
        if (priceText == null || priceText.isBlank() || "Free".equalsIgnoreCase(priceText.trim())) return 0;
        return Double.parseDouble(priceText.replace("DKK", "").trim().replace(",", "."));
    }

    private String formatPrice(String priceText) {
        double price = Double.parseDouble(priceText.trim().replace(",", "."));
        if (price == 0) return "Free";
        if (price == Math.floor(price)) return String.format(Locale.ENGLISH, "%.0f DKK", price);
        return String.format(Locale.ENGLISH, "%.2f DKK", price);
    }
}
