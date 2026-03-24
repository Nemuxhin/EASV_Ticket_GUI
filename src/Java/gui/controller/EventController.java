package Java.gui.controller;

import Java.be.Event;
import Java.bll.EventManager;

import java.util.List;

public class EventController {

    private final EventManager eventManager = new EventManager();

    public List<Event> getEvents() {
        return eventManager.getEvents();
    }

    public void createEvent(Event event) {
        eventManager.createEvent(event);
    }

    public void deleteEvent(Event event) {
        eventManager.deleteEvent(event);
    }

    public void assignCoordinator(Event event, String coordinatorName) {
        eventManager.assignCoordinator(event, coordinatorName);
    }
}