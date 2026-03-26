package Java.Controller;

import Java.Be.Event;
import Java.Be.TicketPurchase;
import Java.Bll.EventManager;

import java.util.List;

public class EventController {
    private final EventManager eventManager = new EventManager();

    public List<Event> getEvents() {
        return eventManager.getEvents();
    }

    public String validateEvent(String title,
                                String date,
                                String time,
                                String venue,
                                String capacity,
                                String price) {
        return eventManager.validateEvent(title, date, time, venue, capacity, price);
    }

    public Event createEvent(String title,
                             String date,
                             String time,
                             String venue,
                             String notes,
                             String price) {
        return eventManager.createEvent(title, date, time, venue, notes, price);
    }

    public void deleteEvent(Event event) {
        eventManager.deleteEvent(event);
    }

    public boolean isValidEmail(String email) {
        return eventManager.isValidEmail(email);
    }

    public String validatePurchase(String customerName,
                                   String customerEmail,
                                   String ticketType,
                                   int quantity) {
        return eventManager.validatePurchase(customerName, customerEmail, ticketType, quantity);
    }

    public double calculateTotalPrice(Event event, String ticketType, int quantity) {
        return eventManager.calculateTotalPrice(event, ticketType, quantity);
    }

    public TicketPurchase createTicketPurchase(Event event,
                                               String customerName,
                                               String customerEmail,
                                               String ticketType,
                                               int quantity) {
        return eventManager.createTicketPurchase(event, customerName, customerEmail, ticketType, quantity);
    }
}
