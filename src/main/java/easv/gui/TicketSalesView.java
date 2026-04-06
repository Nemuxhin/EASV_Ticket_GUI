package easv.gui;

import easv.be.Customer;
import easv.be.Event;
import easv.be.Ticket;
import easv.be.TicketPurchase;
import easv.controller.EventController;
import easv.controller.TicketController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.UUID;

public class TicketSalesView {
    private final MainView mainView;
    private final EventController eventController;
    private final TicketController ticketController;
    private final Event event;

    public TicketSalesView(MainView mainView, EventController eventController, Event event) {
        this.mainView = mainView;
        this.eventController = eventController;
        this.ticketController = new TicketController();
        this.event = event;
    }

    public Parent getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));
        content.getStyleClass().add("main-bg");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Sell Tickets");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("\u2190 Back to Events");
        backButton.getStyleClass().add("primary-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        topBar.getChildren().addAll(title, spacer, backButton);

        VBox eventCard = new VBox(10);
        eventCard.getStyleClass().add("event-card");
        eventCard.getChildren().addAll(
                styleLabel(event.getTitle(), "page-title"),
                styleLabel("Starts: " + event.getStartDateTime(), "card-text"),
                styleLabel("Location: " + event.getLocation(), "card-text")
        );
        if (event.hasEndDateTime()) {
            eventCard.getChildren().add(styleLabel("Ends: " + event.getEndDateTime(), "card-text"));
        }
        if (event.hasLocationGuidance()) {
            eventCard.getChildren().add(styleLabel("Guidance: " + event.getLocationGuidance(), "card-text"));
        }

        VBox formCard = new VBox(18);
        formCard.getStyleClass().add("event-card");
        formCard.setMaxWidth(1100);

        TextField nameField = new TextField();
        nameField.setPromptText("Customer full name");
        nameField.getStyleClass().add("input-field");

        TextField emailField = new TextField();
        emailField.setPromptText("customer@example.com");
        emailField.getStyleClass().add("input-field");

        StringProperty selectedType = new SimpleStringProperty("STANDARD");
        IntegerProperty quantity = new SimpleIntegerProperty(1);

        VBox standardCard = createTicketTypeCard("Standard", calculateTypePrice("STANDARD"), "", true);
        VBox vipCard = createTicketTypeCard("VIP", calculateTypePrice("VIP"), "+50%", false);
        VBox studentCard = createTicketTypeCard("Student", calculateTypePrice("STUDENT"), "-30%", false);

        standardCard.setOnMouseClicked(e -> selectTicketType(selectedType, "STANDARD", standardCard, vipCard, studentCard));
        vipCard.setOnMouseClicked(e -> selectTicketType(selectedType, "VIP", vipCard, standardCard, studentCard));
        studentCard.setOnMouseClicked(e -> selectTicketType(selectedType, "STUDENT", studentCard, standardCard, vipCard));

        Label quantityLabel = styleLabel("1", "card-title");
        quantityLabel.textProperty().bind(quantity.asString());

        Button minus = new Button("-");
        minus.getStyleClass().add("secondary-btn");
        minus.setOnAction(e -> {
            if (quantity.get() > 1) {
                quantity.set(quantity.get() - 1);
            }
        });

        Button plus = new Button("+");
        plus.getStyleClass().add("secondary-btn");
        plus.setOnAction(e -> quantity.set(quantity.get() + 1));

        Label totalLabel = styleLabel(formatPrice(eventController.calculateTotalPrice(event, selectedType.get(), quantity.get())), "page-title");
        Runnable refreshTotal = () -> totalLabel.setText(formatPrice(eventController.calculateTotalPrice(event, selectedType.get(), quantity.get())));
        selectedType.addListener((obs, oldValue, newValue) -> refreshTotal.run());
        quantity.addListener((obs, oldValue, newValue) -> refreshTotal.run());

        Button confirmButton = new Button("Confirm Purchase");
        confirmButton.getStyleClass().add("primary-btn");
        confirmButton.setMaxWidth(Double.MAX_VALUE);
        confirmButton.setOnAction(e -> handlePurchase(nameField.getText().trim(), emailField.getText().trim(), selectedType.get(), quantity.get()));

        formCard.getChildren().addAll(
                createField("Full Name", nameField),
                createField("Email", emailField),
                styleLabel("Select Ticket Type", "notes-head"),
                new HBox(16, standardCard, vipCard, studentCard),
                createField("Quantity", new HBox(12, minus, quantityLabel, plus)),
                new Separator(),
                new HBox(12, styleLabel("Total Price", "card-text"), grow(), totalLabel),
                confirmButton
        );

        content.getChildren().addAll(topBar, eventCard, formCard);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        return scrollPane;
    }

    private VBox createField(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        box.getChildren().addAll(label, field);
        return box;
    }

    private void handlePurchase(String customerName, String customerEmail, String ticketType, int quantity) {
        String validation = eventController.validatePurchase(customerName, customerEmail, ticketType, quantity);
        if (validation != null) {
            AlertHelper.showError("Invalid Purchase", validation);
            return;
        }

        TicketPurchase purchase = eventController.createTicketPurchase(event, customerName, customerEmail, ticketType, quantity);
        String totalPrice = formatPrice(purchase.getTotalPrice());

        Customer customer = new Customer(
                "CUST-" + UUID.randomUUID().toString().substring(0, 8),
                purchase.getCustomerName(),
                purchase.getCustomerEmail()
        );

        Ticket ticket = ticketController.createEventTicket(
                event,
                customer,
                purchase.getTicketType(),
                describeTicketType(purchase.getTicketType()),
                totalPrice,
                event.getEndDateTime(),
                event.getLocationGuidance()
        );

        showSuccess(ticket, purchase, totalPrice);
    }

    private void showSuccess(Ticket ticket, TicketPurchase purchase, String totalPaid) {
        VBox content = new VBox(18);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("main-bg");

        VBox card = new VBox(14);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(24));
        card.setMaxWidth(860);
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Purchase Successful");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Your ticket was generated successfully.");
        subtitle.getStyleClass().add("card-text");

        ImageView qrView = new ImageView(new Image(new ByteArrayInputStream(ticket.getQrImage())));
        qrView.setFitWidth(180);
        qrView.setFitHeight(180);
        qrView.setPreserveRatio(true);

        ImageView barcodeView = new ImageView(new Image(new ByteArrayInputStream(ticket.getBarcodeImage())));
        barcodeView.setFitWidth(340);
        barcodeView.setFitHeight(90);
        barcodeView.setPreserveRatio(true);

        Label details = new Label(
                "Ticket ID: " + ticket.getTicketId() + "\n" +
                "Customer: " + ticket.getCustomer().getName() + "\n" +
                "Email: " + ticket.getCustomer().getEmail() + "\n" +
                "Event: " + ticket.getEventTitle() + "\n" +
                "Ticket type: " + formatTicketType(purchase.getTicketType()) + "\n" +
                "Quantity: " + purchase.getQuantity() + "\n" +
                "Total paid: " + totalPaid
        );
        details.getStyleClass().add("card-text");
        details.setWrapText(true);
        details.setMaxWidth(540);

        Button backButton = new Button("Back to Events");
        backButton.getStyleClass().add("primary-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        card.getChildren().addAll(
                title,
                subtitle,
                details,
                new Label("QR Code"),
                qrView,
                new Label("Barcode"),
                barcodeView,
                backButton
        );

        StackPane centered = new StackPane(card);
        centered.setPadding(new Insets(30));

        content.getChildren().add(centered);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        mainView.setContent(scrollPane);
    }

    private VBox createTicketTypeCard(String title, String price, String modifier, boolean selected) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18));
        card.getStyleClass().add(selected ? "ticket-type-selected" : "ticket-type");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        card.getChildren().addAll(
                styleLabel(title, "card-title"),
                styleLabel(price, "card-text")
        );
        if (!modifier.isBlank()) {
            card.getChildren().add(styleLabel(modifier, "card-text"));
        }
        return card;
    }

    private void selectTicketType(StringProperty selectedType, String nextType, VBox selectedCard, VBox otherCardA, VBox otherCardB) {
        selectedType.set(nextType);
        selectedCard.getStyleClass().setAll("ticket-type-selected");
        otherCardA.getStyleClass().setAll("ticket-type");
        otherCardB.getStyleClass().setAll("ticket-type");
    }

    private String calculateTypePrice(String type) {
        return formatPrice(eventController.calculateTotalPrice(event, type, 1));
    }

    private String describeTicketType(String ticketType) {
        return switch (ticketType.toUpperCase(Locale.ENGLISH)) {
            case "VIP" -> "VIP access";
            case "STUDENT" -> "Student access";
            default -> "Standard access";
        };
    }

    private String formatTicketType(String ticketType) {
        return switch (ticketType.toUpperCase(Locale.ENGLISH)) {
            case "VIP" -> "VIP";
            case "STUDENT" -> "Student";
            default -> "Standard";
        };
    }

    private String formatPrice(double amount) {
        if (amount == 0) {
            return "Free";
        }
        if (amount == Math.floor(amount)) {
            return String.format(Locale.ENGLISH, "%.0f DKK", amount);
        }
        return String.format(Locale.ENGLISH, "%.2f DKK", amount);
    }

    private Label styleLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        return label;
    }

    private Region grow() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }
}
