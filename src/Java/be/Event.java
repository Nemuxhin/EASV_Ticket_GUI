package Java.be;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Event {

    private String eventId;
    private String title;
    private String date;
    private String location;
    private String notes;
    private String status;
    private List<String> coordinators;
    private List<TicketOption> ticketOptions;

    public Event(String title, String date, String location, String notes, String status) {
        this(
                UUID.randomUUID().toString(),
                title,
                date,
                location,
                notes,
                status,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public Event(String title, String date, String location, String notes, String status,
                 List<String> coordinators, List<TicketOption> ticketOptions) {
        this(
                UUID.randomUUID().toString(),
                title,
                date,
                location,
                notes,
                status,
                coordinators,
                ticketOptions
        );
    }

    public Event(String eventId, String title, String date, String location, String notes, String status,
                 List<String> coordinators, List<TicketOption> ticketOptions) {
        this.eventId = eventId;
        this.title = title;
        this.date = date;
        this.location = location;
        this.notes = notes;
        this.status = status;
        this.coordinators = coordinators != null ? coordinators : new ArrayList<>();
        this.ticketOptions = ticketOptions != null ? ticketOptions : new ArrayList<>();
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getCoordinators() {
        return coordinators;
    }

    public List<TicketOption> getTicketOptions() {
        return ticketOptions;
    }

    public List<TicketOption> getActiveTicketOptions() {
        List<TicketOption> active = new ArrayList<>();
        for (TicketOption option : ticketOptions) {
            if (option.isActive()) {
                active.add(option);
            }
        }
        return active;
    }

    public void addTicketOption(TicketOption option) {
        if (option != null) {
            ticketOptions.add(option);
        }
    }

    public void removeTicketOptionById(String optionId) {
        ticketOptions.removeIf(option -> option.getOptionId().equals(optionId));
    }

    public String getStartingPriceText() {
        List<TicketOption> active = getActiveTicketOptions();
        if (active.isEmpty()) {
            return "No ticket types";
        }

        int lowest = active.get(0).getPriceInDkk();
        for (TicketOption option : active) {
            if (option.getPriceInDkk() < lowest) {
                lowest = option.getPriceInDkk();
            }
        }

        return lowest <= 0 ? "Free" : lowest + " DKK";
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCoordinators(List<String> coordinators) {
        this.coordinators = coordinators;
    }

    public void setTicketOptions(List<TicketOption> ticketOptions) {
        this.ticketOptions = ticketOptions;
    }

    public static class TicketOption {
        private String optionId;
        private String name;
        private String description;
        private int priceInDkk;
        private boolean active;

        public TicketOption(String name, String description, int priceInDkk) {
            this(UUID.randomUUID().toString(), name, description, priceInDkk, true);
        }

        public TicketOption(String optionId, String name, String description, int priceInDkk, boolean active) {
            this.optionId = optionId;
            this.name = name;
            this.description = description;
            this.priceInDkk = priceInDkk;
            this.active = active;
        }

        public String getOptionId() {
            return optionId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getPriceInDkk() {
            return priceInDkk;
        }

        public boolean isActive() {
            return active;
        }

        public String getPriceText() {
            return priceInDkk <= 0 ? "Free" : priceInDkk + " DKK";
        }

        public void setOptionId(String optionId) {
            this.optionId = optionId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPriceInDkk(int priceInDkk) {
            this.priceInDkk = priceInDkk;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}