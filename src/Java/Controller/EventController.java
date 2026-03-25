package Java.Controller;

import Java.Be.Event;
import Java.Bll.EventManager;
import java.util.List;

public class EventController {
    private final EventManager eventManager = new EventManager();

    public List<Event> getEvents() {
        return eventManager.getEvents();
    }

    public void addEvent(Event event) {
        eventManager.addEvent(event);
    }

    public boolean updateEvent(Event currentEvent, Event updatedEvent) {
        return eventManager.updateEvent(currentEvent, updatedEvent);
    }

    public boolean deleteEvent(Event event) {
        return eventManager.deleteEvent(event);
    }
}
