package Java.Be;

public class Event {
    private String title, date, location, notes, price, status;
    private String[] coordinators;

    public Event(String t, String d, String l, String n, String p, String s, String[] c) {
        this.title = t; this.date = d; this.location = l;
        this.notes = n; this.price = p; this.status = s;
        this.coordinators = c;
    }

    // Getters for the UI
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
    public String[] getCoordinators() { return coordinators; }
}
