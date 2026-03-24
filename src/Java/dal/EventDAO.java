package Java.dal;

import Java.be.Event;

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
                    "EASV Campus, Esbjerg",
                    "Annual graduation ceremony for EASV students",
                    "Available",
                    new ArrayList<>(Arrays.asList("Event Coordinator 1", "Event Coordinator 2")),
                    new ArrayList<>(Arrays.asList(
                            new Event.TicketOption("Standard", "Regular seat", 0),
                            new Event.TicketOption("VIP", "Priority seating + welcome drink", 225),
                            new Event.TicketOption("1st Row", "Reserved front-row seating", 300)
                    ))
            ));

            EVENTS.add(new Event(
                    "Tech Innovation Summit",
                    "15 Jul 2026 at 09:00",
                    "Innovation Hub, Esbjerg",
                    "Annual technology and innovation conference",
                    "Selling Fast",
                    new ArrayList<>(Arrays.asList("Event Coordinator 3")),
                    new ArrayList<>(Arrays.asList(
                            new Event.TicketOption("Standard", "Conference access", 150),
                            new Event.TicketOption("Food Included", "Conference access + lunch", 220),
                            new Event.TicketOption("VIP", "VIP access + front seating", 350)
                    ))
            ));

            EVENTS.add(new Event(
                    "Danish Business Networking",
                    "22 Aug 2026 at 18:00",
                    "Copenhagen Convention Center",
                    "Business networking event for professionals",
                    "Available",
                    new ArrayList<>(Arrays.asList("Event Coordinator 4", "Event Coordinator 5")),
                    new ArrayList<>(Arrays.asList(
                            new Event.TicketOption("Standard", "Entry ticket", 500),
                            new Event.TicketOption("Food Included", "Entry + dinner buffet", 650),
                            new Event.TicketOption("Free Beer Included", "Entry + one included beer", 575)
                    ))
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

    public void assignCoordinatorToEvent(Event event, String coordinatorName) {
        if (!event.getCoordinators().contains(coordinatorName)) {
            event.getCoordinators().add(coordinatorName);
        }
    }

    public void addTicketOptionToEvent(Event event, Event.TicketOption option) {
        event.addTicketOption(option);
    }

    public void removeTicketOptionFromEvent(Event event, String optionId) {
        event.removeTicketOptionById(optionId);
    }
}