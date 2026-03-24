package Java.bll;

import Java.be.Event;
import Java.dal.EventDAO;

import java.util.List;

public class EventManager {

    private final EventDAO eventDAO = new EventDAO();

    public List<Event> getEvents() {
        return eventDAO.getAllEvents();
    }

    public void createEvent(Event event) {
        eventDAO.addEvent(event);
    }

    public void deleteEvent(Event event) {
        eventDAO.deleteEvent(event);
    }

    public void assignCoordinator(Event event, String coordinatorName) {
        eventDAO.assignCoordinator(event, coordinatorName);
    }
}