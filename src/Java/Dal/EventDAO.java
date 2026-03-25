package Java.Dal;

import Java.Be.Event;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private final List<Event> events;

    public EventDAO() {
        events = new ArrayList<>();
        loadSampleEvents();
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    // SAMU: New events are saved in the same shared list used by every overview.
    public void addEvent(Event event) {
        events.add(event);
    }

    // SAMU: Delete returns false when the selected event is not found.
    public boolean deleteEvent(Event event) {
        return events.remove(event);
    }

    private void loadSampleEvents() {
        events.add(new Event(
                "EASV Graduation Ceremony 2026",
                "20 Jun 2026 at 14:00",
                "20 Jun 2026 at 16:00",
                "EASV Campus, Esbjerg",
                "Use the main hall entrance near the parking area.",
                "Annual graduation ceremony for EASV students",
                "Free",
                "Available",
                new String[]{"Event Coordinator 1", "Event Coordinator 2"}
        ));

        events.add(new Event(
                "Tech Innovation Summit",
                "15 Jul 2026 at 09:00",
                "",
                "Innovation Hub, Esbjerg",
                "",
                "Annual technology and innovation conference",
                "150 DKK",
                "Selling Fast",
                new String[]{"Event Coordinator 3"}
        ));

        events.add(new Event(
                "Danish Business Networking",
                "22 Aug 2026 at 18:00",
                "22 Aug 2026 at 21:00",
                "Copenhagen Convention Center",
                "Meet at the north lobby reception desk.",
                "Business networking event for professionals",
                "500 DKK",
                "Available",
                new String[]{"Event Coordinator 4", "Event Coordinator 5"}
        ));
    }
}
