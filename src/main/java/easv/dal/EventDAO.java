package easv.dal;

import easv.be.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventDAO {
    private static final List<Event> EVENTS = new ArrayList<>();

    static {
        if (EVENTS.isEmpty()) {
            EVENTS.add(new Event(
                    "EASV Graduation Ceremony 2026",
                    "20 Jun 2026 at 14:00",
                    "20 Jun 2026 at 17:00",
                    "EASV Campus, Esbjerg",
                    "Use the main hall entrance near the parking area.",
                    "Annual graduation ceremony for EASV students",
                    "Free",
                    "Available",
                    new String[]{"Sarah Jensen", "Mikkel Andersen"}
            ));

            EVENTS.add(new Event(
                    "Tech Innovation Summit",
                    "15 Jul 2026 at 09:00",
                    "15 Jul 2026 at 16:00",
                    "Innovation Hub, Esbjerg",
                    "Follow the signs to the conference wing.",
                    "Annual technology and innovation conference",
                    "150 DKK",
                    "Selling Fast",
                    new String[]{"Laura Nielsen"}
            ));

            EVENTS.add(new Event(
                    "Danish Business Networking",
                    "22 Aug 2026 at 18:00",
                    "22 Aug 2026 at 22:00",
                    "Copenhagen Convention Center",
                    "Registration desk is inside the main lobby.",
                    "Business networking event for professionals",
                    "500 DKK",
                    "Available",
                    new String[]{"Peter Christiansen"}
            ));
        }
    }

    public List<Event> getAllEvents() { return new ArrayList<>(EVENTS); }
    public void addEvent(Event event) { EVENTS.add(event); }
    public void deleteEvent(Event event) { EVENTS.remove(event); }

    public boolean updateEvent(Event currentEvent, Event updatedEvent) {
        int index = EVENTS.indexOf(currentEvent);
        if (index < 0) return false;
        EVENTS.set(index, updatedEvent);
        return true;
    }

    public void assignCoordinator(Event event, String coordinatorName) {
        String[] current = event.getCoordinators();
        for (String coordinator : current) {
            if (coordinator.equalsIgnoreCase(coordinatorName)) return;
        }
        String[] updated = Arrays.copyOf(current, current.length + 1);
        updated[current.length] = coordinatorName;
        event.setCoordinators(updated);
    }

    public void setCoordinators(Event event, String[] coordinators) {
        event.setCoordinators(Arrays.copyOf(coordinators, coordinators.length));
    }
}
