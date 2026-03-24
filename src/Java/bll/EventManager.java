package Java.bll;

import Java.be.Event;
import Java.dal.EventDAO;

import java.util.List;

public class EventManager {

    private final EventDAO eventDAO;

    public EventManager() {
        this.eventDAO = new EventDAO();
    }

    public List<Event> getAllEvents() {
        return eventDAO.getAllEvents();
    }

    public void createEvent(Event event) {
        eventDAO.addEvent(event);
    }

    public void deleteEvent(Event event) {
        eventDAO.deleteEvent(event);
    }

    public void assignCoordinatorToEvent(Event event, String coordinatorName) {
        eventDAO.assignCoordinatorToEvent(event, coordinatorName);
    }

    public void addTicketOptionToEvent(Event event, Event.TicketOption option) {
        eventDAO.addTicketOptionToEvent(event, option);
    }

    public void removeTicketOptionFromEvent(Event event, String optionId) {
        eventDAO.removeTicketOptionFromEvent(event, optionId);
    }
}