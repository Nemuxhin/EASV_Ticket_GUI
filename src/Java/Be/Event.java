package Java.Be;

public class Event {
    private final String title;
    private final String startDateTime;
    private final String endDateTime;
    private final String location;
    private final String locationGuidance;
    private final String notes;
    private final String price;
    private final String status;
    private final String[] coordinators;

    // SAMU: This model keeps required and optional event data together.
    public Event(String title, String startDateTime, String endDateTime, String location,
                 String locationGuidance, String notes, String price, String status,
                 String[] coordinators) {
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.locationGuidance = locationGuidance;
        this.notes = notes;
        this.price = price;
        this.status = status;
        this.coordinators = coordinators == null ? new String[0] : coordinators;
    }

    public String getTitle() {
        return title;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public String getLocation() {
        return location;
    }

    public String getLocationGuidance() {
        return locationGuidance;
    }

    public String getNotes() {
        return notes;
    }

    public String getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String[] getCoordinators() {
        return coordinators;
    }

    // SAMU: Optional values are shown only when they exist.
    public boolean hasEndDateTime() {
        return endDateTime != null && !endDateTime.isBlank();
    }

    public boolean hasLocationGuidance() {
        return locationGuidance != null && !locationGuidance.isBlank();
    }
}
