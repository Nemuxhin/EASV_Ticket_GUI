package Java.Bll;

import Java.Be.Event;
import Java.Dal.EventDAO;
import java.util.List;

public class EventManager {
    private EventDAO eventDAO = new EventDAO();
    public List<Event> getEvents() { return eventDAO.getAllEvents(); }
}