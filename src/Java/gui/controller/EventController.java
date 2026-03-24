package Java.controller;

import Java.be.Event;
import Java.bll.EventManager;

import java.util.List;

public class EventController {

    private final EventManager eventManager;

    public EventController() {
        this.eventManager = new EventManager();
    }

    public List<Event> getAllEvents() {
        return eventManager.getAllEvents();
    }

    public void createEvent(Event event) {
        eventManager.createEvent(event);
    }

    public void deleteEvent(Event event) {
        eventManager.deleteEvent(event);
    }

    public void assignCoordinatorToEvent(Event event, String coordinatorName) {
        eventManager.assignCoordinatorToEvent(event, coordinatorName);
    }

    public void addTicketOptionToEvent(Event event, Event.TicketOption option) {
        eventManager.addTicketOptionToEvent(event, option);
    }

    public void removeTicketOptionFromEvent(Event event, String optionId) {
        eventManager.removeTicketOptionFromEvent(event, optionId);
    }
}