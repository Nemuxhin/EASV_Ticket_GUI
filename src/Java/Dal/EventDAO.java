package Java.Dal;

import Java.Be.Event;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        events.add(new Event("EASV Graduation Ceremony 2026", "20 Jun 2026 at 14:00", "EASV Campus, Esbjerg", "Annual graduation ceremony for EASV students", "Free", "Available", new String[]{"Event Coordinator 1", "Event Coordinator 2"}));
        events.add(new Event("Tech Innovation Summit", "15 Jul 2026 at 09:00", "Innovation Hub, Esbjerg", "Annual technology and innovation conference", "150 DKK", "Selling Fast", new String[]{"Event Coordinator 3"}));
        events.add(new Event("Danish Business Networking", "22 Aug 2026 at 18:00", "Copenhagen Convention Center", "Business networking event for professionals", "500 DKK", "Available", new String[]{"Event Coordinator 4", "Event Coordinator 5"}));
        return events;
    }
}