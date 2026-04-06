package easv.be;

public class Event {
    private String title;
    private String startDateTime;
    private String endDateTime;
    private String location;
    private String locationGuidance;
    private String notes;
    private String price;
    private String status;
    private String[] coordinators;

    public Event(String title, String date, String location, String notes, String price, String status, String[] coordinators) {
        this(title, date, "", location, "", notes, price, status, coordinators);
    }

    public Event(String title,
                 String startDateTime,
                 String endDateTime,
                 String location,
                 String locationGuidance,
                 String notes,
                 String price,
                 String[] coordinators) {
        this(title, startDateTime, endDateTime, location, locationGuidance, notes, price, "Available", coordinators);
    }

    public Event(String title,
                 String startDateTime,
                 String endDateTime,
                 String location,
                 String locationGuidance,
                 String notes,
                 String price,
                 String status,
                 String[] coordinators) {
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime == null ? "" : endDateTime;
        this.location = location;
        this.locationGuidance = locationGuidance == null ? "" : locationGuidance;
        this.notes = notes == null ? "" : notes;
        this.price = price;
        this.status = status == null ? "Available" : status;
        this.coordinators = coordinators == null ? new String[0] : coordinators;
    }

    public String getTitle() { return title; }
    public String getDate() { return startDateTime; }
    public String getStartDateTime() { return startDateTime; }
    public String getEndDateTime() { return endDateTime; }
    public String getLocation() { return location; }
    public String getLocationGuidance() { return locationGuidance; }
    public String getNotes() { return notes; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
    public String[] getCoordinators() { return coordinators; }

    public boolean hasEndDateTime() { return endDateTime != null && !endDateTime.isBlank(); }
    public boolean hasLocationGuidance() { return locationGuidance != null && !locationGuidance.isBlank(); }

    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.startDateTime = date; }
    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }
    public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime == null ? "" : endDateTime; }
    public void setLocation(String location) { this.location = location; }
    public void setLocationGuidance(String locationGuidance) { this.locationGuidance = locationGuidance == null ? "" : locationGuidance; }
    public void setNotes(String notes) { this.notes = notes == null ? "" : notes; }
    public void setPrice(String price) { this.price = price; }
    public void setStatus(String status) { this.status = status == null ? "Available" : status; }
    public void setCoordinators(String[] coordinators) { this.coordinators = coordinators == null ? new String[0] : coordinators; }
}
