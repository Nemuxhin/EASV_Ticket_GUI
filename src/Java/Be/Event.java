package Java.Be;

public class Event {
    private String title;
    private String date;
    private String location;
    private String notes;
    private String price;
    private String status;
    private String[] coordinators;

    public Event(String title, String date, String location, String notes,
                 String price, String status, String[] coordinators) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.notes = notes;
        this.price = price;
        this.status = status;
        this.coordinators = coordinators;
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

    public String getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String[] getCoordinators() {
        return coordinators;
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

    public void setPrice(String price) {
        this.price = price;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCoordinators(String[] coordinators) {
        this.coordinators = coordinators;
    }
}