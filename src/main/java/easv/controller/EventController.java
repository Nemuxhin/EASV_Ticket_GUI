package easv.controller;

import easv.be.Event;
import easv.be.TicketPurchase;
import easv.bll.EventManager;

import java.util.List;

public class EventController {
    private final EventManager eventManager = new EventManager();

    public List<Event> getEvents() { return eventManager.getEvents(); }
    public List<Event> getArchivedEvents() { return eventManager.getArchivedEvents(); }
    public void createEvent(Event event) { eventManager.createEvent(event); }
    public void addEvent(Event event) { eventManager.addEvent(event); }
    public boolean updateEvent(Event currentEvent, Event updatedEvent) { return eventManager.updateEvent(currentEvent, updatedEvent); }
    public void deleteEvent(Event event) { eventManager.deleteEvent(event); }
    public void restoreEvent(Event event) { eventManager.restoreEvent(event); }
    public void assignCoordinator(Event event, String coordinatorName) { eventManager.assignCoordinator(event, coordinatorName); }
    public void setCoordinators(Event event, String[] coordinators) { eventManager.setCoordinators(event, coordinators); }
    public void updateEventStatus(Event event, String status) { eventManager.updateEventStatus(event, status); }
    public String validateEvent(String title, String date, String time, String venue, String capacity, String price) { return eventManager.validateEvent(title, date, time, venue, capacity, price); }
    public Event createEvent(String title, String date, String time, String venue, String notes, String price) { return eventManager.createEvent(title, date, time, venue, notes, price); }
    public boolean isValidEmail(String email) { return eventManager.isValidEmail(email); }
    public String validatePurchase(String customerName, String customerEmail, String ticketType, int quantity) { return eventManager.validatePurchase(customerName, customerEmail, ticketType, quantity); }
    public double calculateTotalPrice(Event event, String ticketType, int quantity) { return eventManager.calculateTotalPrice(event, ticketType, quantity); }
    public TicketPurchase createTicketPurchase(Event event, String customerName, String customerEmail, String ticketType, int quantity) { return eventManager.createTicketPurchase(event, customerName, customerEmail, ticketType, quantity); }
}
