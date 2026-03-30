package easv.gui;

import easv.be.Customer;
import easv.be.Event;
import easv.be.Ticket;
import easv.controller.TicketController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;

public class TicketPreviewTestApp extends Application {

    @Override
    public void start(Stage stage) {
        TicketController ticketController = new TicketController();

        Customer customer = new Customer(
                "CUST-001",
                "Anna Jensen",
                "anna@email.com"
        );

        Event event = new Event(
                "EASV Party",
                "25 May 2026 at 19:00",
                "EASV Campus, Esbjerg",
                "Bring student ID at entrance",
                "150 DKK",
                "Available",
                new String[]{"Sarah Jensen"}
        );

        Ticket ticket = ticketController.createEventTicket(
                event,
                customer,
                "VIP",
                "Front section access",
                "250 DKK",
                "25 May 2026 at 23:30",
                "Use the main entrance near parking area"
        );
        Image qr = new Image(new ByteArrayInputStream(ticket.getQrImage()));
        Image barcode = new Image(new ByteArrayInputStream(ticket.getBarcodeImage()));

        ImageView qrView = new ImageView(qr);
        qrView.setFitWidth(220);
        qrView.setFitHeight(220);
        qrView.setPreserveRatio(true);

        ImageView barcodeView = new ImageView(barcode);
        barcodeView.setFitWidth(340);
        barcodeView.setFitHeight(100);
        barcodeView.setPreserveRatio(true);

        Label title = new Label("Ticket Backend Test");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label ticketIdLbl = new Label("Ticket ID: " + ticket.getTicketId());
        Label tokenLbl = new Label("Secure Token: " + ticket.getSecureToken());
        Label customerLbl = new Label("Customer: " + ticket.getCustomer().getName() + " (" + ticket.getCustomer().getEmail() + ")");
        Label eventLbl = new Label("Event: " + ticket.getEventTitle());
        Label typeLbl = new Label("Type: " + ticket.getTicketType());
        Label priceLbl = new Label("Price: " + ticket.getPrice());
        Label usedLbl = new Label("Used: " + ticket.isUsed());

        boolean validBefore = ticketController.isTicketValid(ticket.getSecureToken());
        boolean markedUsed = ticketController.markTicketAsUsed(ticket.getSecureToken());
        boolean validAfter = ticketController.isTicketValid(ticket.getSecureToken());
        Label validBeforeLbl = new Label("Valid before use: " + validBefore);
        Label markedUsedLbl = new Label("Marked used: " + markedUsed);
        Label validAfterLbl = new Label("Valid after use: " + validAfter);

        VBox root = new VBox(
                12,
                title,
                ticketIdLbl,
                tokenLbl,
                customerLbl,
                eventLbl,
                typeLbl,
                priceLbl,
                usedLbl,
                validBeforeLbl,
                markedUsedLbl,
                validAfterLbl,
                new Separator(),
                new Label("QR Code"),
                qrView,
                new Label("1D Barcode"),
                barcodeView
        );

        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 520, 820);

        stage.setTitle("Ticket Preview Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}