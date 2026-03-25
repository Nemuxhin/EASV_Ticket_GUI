package Java.Bll;

import Java.Be.Event;
import Java.Dal.EventDAO;
import java.util.List;

public class EventManager {
    private final EventDAO eventDAO = new EventDAO();

    public List<Event> getEvents() {
        return eventDAO.getAllEvents();
    }

    public void addEvent(Event event) {
        eventDAO.addEvent(event);
    }

    public boolean updateEvent(Event currentEvent, Event updatedEvent) {
        return eventDAO.updateEvent(currentEvent, updatedEvent);
    }

    public boolean deleteEvent(Event event) {
        return eventDAO.deleteEvent(event);
    }
}
