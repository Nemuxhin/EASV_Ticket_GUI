package easv.dal;

import easv.be.Event;

import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    private static final List<Event> EVENTS = new ArrayList<>();

    static {
        if (EVENTS.isEmpty()) {
            EVENTS.add(new Event(
                    "EASV Graduation Ceremony 2026",
                    "20 Jun 2026 at 14:00",
                    "EASV Campus, Esbjerg",
                    "Annual graduation ceremony for EASV students",
                    "Free",
                    "Available",
                    new String[]{"Event Coordinator 1", "Event Coordinator 2"}
            ));

            EVENTS.add(new Event(
                    "Tech Innovation Summit",
                    "15 Jul 2026 at 09:00",
                    "Innovation Hub, Esbjerg",
                    "Annual technology and innovation conference",
                    "150 DKK",
                    "Selling Fast",
                    new String[]{"Event Coordinator 3"}
            ));

            EVENTS.add(new Event(
                    "Danish Business Networking",
                    "22 Aug 2026 at 18:00",
                    "Copenhagen Convention Center",
                    "Business networking event for professionals",
                    "500 DKK",
                    "Available",
                    new String[]{"Event Coordinator 4", "Event Coordinator 5"}
            ));
        }
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(EVENTS);
    }

    public void addEvent(Event event) {
        EVENTS.add(event);
    }

    public void deleteEvent(Event event) {
        EVENTS.remove(event);
    }

    public void assignCoordinator(Event event, String coordinatorName) {
        String[] current = event.getCoordinators();

        for (String coordinator : current) {
            if (coordinator.equalsIgnoreCase(coordinatorName)) {
                return;
            }
        }

        String[] updated = new String[current.length + 1];
        System.arraycopy(current, 0, updated, 0, current.length);
        updated[current.length] = coordinatorName;
        event.setCoordinators(updated);
    }
}