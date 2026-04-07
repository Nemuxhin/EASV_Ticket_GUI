package easv.gui;

import easv.be.Event;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class MapViewHelper {
    private MapViewHelper() {
    }

    public static void openDirections(Event event) {
        if (event == null) {
            AlertHelper.showError("Directions", "No event was selected.");
            return;
        }

        String destination = event.getLocation();
        if (destination == null || destination.isBlank()) {
            AlertHelper.showError("Directions", "This event does not have a stored address yet.");
            return;
        }

        openDirections(destination);
    }

    public static void openDirections(String destinationAddress) {
        if (destinationAddress == null || destinationAddress.isBlank()) {
            AlertHelper.showError("Directions", "A destination address is required.");
            return;
        }

        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            AlertHelper.showError("Directions", "Opening Google Maps is not supported on this computer.");
            return;
        }

        String encodedDestination = URLEncoder.encode(destinationAddress, StandardCharsets.UTF_8);
        String mapsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + encodedDestination + "&travelmode=driving";

        try {
            Desktop.getDesktop().browse(URI.create(mapsUrl));
        } catch (IOException ex) {
            AlertHelper.showError("Directions", "Google Maps could not be opened.");
        }
    }
}
