package Java.Controller;

import Java.Be.Event;
import Java.Bll.EventManager;
import java.util.List;

public class EventController {
    private EventManager eventManager = new EventManager();
    public List<Event> getEvents() { return eventManager.getEvents(); }
}