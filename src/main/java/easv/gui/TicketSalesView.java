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

        content.getChildren().addAll(
                buildTopBar(),
                buildEventCard(),
                buildFormCard()
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        return scrollPane;
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Sell Tickets");
        title.getStyleClass().add("page-title");

        Button backButton = new Button("\u2190 Back to Events");
        backButton.getStyleClass().add("primary-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        topBar.getChildren().addAll(title, grow(), backButton);
        return topBar;
    }

    private VBox buildEventCard() {
        VBox eventCard = new VBox(10);
        eventCard.getStyleClass().add("event-card");

        eventCard.getChildren().addAll(
                styleLabel(event.getTitle(), "page-title"),
                styleLabel("Starts: " + safeText(event.getStartDateTime()), "card-text"),
                styleLabel("Location: " + safeText(event.getLocation()), "card-text")
        );

        if (event.hasEndDateTime()) {
            eventCard.getChildren().add(styleLabel("Ends: " + safeText(event.getEndDateTime()), "card-text"));
        }

        if (event.hasLocationGuidance()) {
            eventCard.getChildren().add(styleLabel("Guidance: " + safeText(event.getLocationGuidance()), "card-text"));
        }

        return eventCard;
    }

    private VBox buildFormCard() {
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

        HBox ticketTypeRow = new HBox(16, standardCard, vipCard, studentCard);

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

        HBox quantityBox = new HBox(12, minus, quantityLabel, plus);
        quantityBox.setAlignment(Pos.CENTER_LEFT);

        Label totalLabel = styleLabel(
                formatPrice(eventController.calculateTotalPrice(event, selectedType.get(), quantity.get())),
                "page-title"
        );

        Runnable refreshTotal = () -> totalLabel.setText(
                formatPrice(eventController.calculateTotalPrice(event, selectedType.get(), quantity.get()))
        );

        selectedType.addListener((obs, oldValue, newValue) -> refreshTotal.run());
        quantity.addListener((obs, oldValue, newValue) -> refreshTotal.run());

        Button confirmButton = new Button("Confirm Purchase");
        confirmButton.getStyleClass().add("primary-btn");
        confirmButton.setMaxWidth(Double.MAX_VALUE);
        confirmButton.setOnAction(e ->
                handlePurchase(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        selectedType.get(),
                        quantity.get()
                )
        );

        formCard.getChildren().addAll(
                createField("Full Name", nameField),
                createField("Email", emailField),
                styleLabel("Select Ticket Type", "notes-head"),
                ticketTypeRow,
                createField("Quantity", quantityBox),
                new Separator(),
                new HBox(12, styleLabel("Total Price", "card-text"), grow(), totalLabel),
                confirmButton
        );

        return formCard;
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

        TicketPurchase purchase = eventController.createTicketPurchase(
                event,
                customerName,
                customerEmail,
                ticketType,
                quantity
        );

        Ticket ticket = createTicketFromPurchase(purchase);
        showSuccess(ticket, purchase, formatPrice(purchase.getTotalPrice()));
    }

    private Ticket createTicketFromPurchase(TicketPurchase purchase) {
        Customer customer = new Customer(
                "CUST-" + UUID.randomUUID().toString().substring(0, 8),
                purchase.getCustomerName(),
                purchase.getCustomerEmail()
        );

        return ticketController.createEventTicket(
                event,
                customer,
                purchase.getTicketType(),
                describeTicketType(purchase.getTicketType()),
                formatPrice(purchase.getTotalPrice()),
                event.getEndDateTime(),
                event.getLocationGuidance()
        );
    }

    private void showSuccess(Ticket ticket, TicketPurchase purchase, String totalPaid) {
        VBox page = new VBox();
        page.setAlignment(Pos.TOP_CENTER);
        page.setPadding(new Insets(14));
        page.getStyleClass().add("main-bg");

        VBox card = new VBox(20);
        card.setMaxWidth(760);
        card.setPadding(new Insets(24));
        card.getStyleClass().addAll("event-card", "purchase-success-card");

        card.getChildren().addAll(
                buildSuccessHeader(),
                new Separator(),
                buildSuccessBody(ticket, purchase, totalPaid),
                buildTicketIdStrip(ticket),
                buildSuccessBackButton()
        );

        page.getChildren().add(card);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        mainView.setContent(scrollPane);
    }

    private VBox buildSuccessHeader() {
        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().add("success-icon-circle");
        iconCircle.setPrefSize(64, 64);

        Label icon = new Label("✓");
        icon.getStyleClass().add("success-icon");
        iconCircle.getChildren().add(icon);

        Label title = new Label("Purchase Successful!");
        title.getStyleClass().add("success-title");

        Label subtitle = new Label("Your ticket has been generated");
        subtitle.getStyleClass().add("success-subtitle");

        VBox header = new VBox(10, iconCircle, title, subtitle);
        header.setAlignment(Pos.CENTER);
        return header;
    }

    private HBox buildSuccessBody(Ticket ticket, TicketPurchase purchase, String totalPaid) {
        VBox leftColumn = buildLeftDetailsColumn(ticket);
        VBox rightColumn = buildRightQrColumn(ticket, purchase, totalPaid);

        HBox body = new HBox(24, leftColumn, rightColumn);
        body.setAlignment(Pos.TOP_LEFT);

        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        return body;
    }

    private VBox buildLeftDetailsColumn(Ticket ticket) {
        Label eventDetailsTitle = new Label("Event Details");
        eventDetailsTitle.getStyleClass().add("success-section-title");

        VBox eventInfo = new VBox(
                12,
                createLabeledValue("Event Name", safeText(ticket.getEventTitle())),
                createIconRow("📅", extractDate(ticket.getEventStartDateTime())),
                createIconRow("🕒", extractTime(ticket.getEventStartDateTime())),
                createIconRow("📍", safeText(ticket.getEventLocation()))
        );

        Label customerInfoTitle = new Label("Customer Information");
        customerInfoTitle.getStyleClass().add("success-section-title");

        String customerName = ticket.getCustomer() != null ? ticket.getCustomer().getName() : "-";
        String customerEmail = ticket.getCustomer() != null ? ticket.getCustomer().getEmail() : "-";

        VBox customerInfo = new VBox(
                12,
                createIconRow("👤", safeText(customerName)),
                createIconRow("✉", safeText(customerEmail))
        );

        VBox left = new VBox(
                18,
                eventDetailsTitle,
                eventInfo,
                new Separator(),
                customerInfoTitle,
                customerInfo
        );

        left.setPrefWidth(360);
        return left;
    }

    private VBox buildRightQrColumn(Ticket ticket, TicketPurchase purchase, String totalPaid) {
        ImageView qrView = new ImageView(new Image(new ByteArrayInputStream(ticket.getQrImage())));
        qrView.setFitWidth(200);
        qrView.setFitHeight(200);
        qrView.setPreserveRatio(true);

        VBox qrBox = new VBox(10);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.getStyleClass().add("ticket-qr-box");

        Label qrHint = new Label("Scan this QR code at the venue");
        qrHint.getStyleClass().add("qr-hint");

        qrBox.getChildren().addAll(qrView, qrHint);

        VBox summaryBox = new VBox(
                14,
                createSummaryRow("Ticket Type:", formatTicketType(purchase.getTicketType()), false),
                createSummaryRow("Quantity:", String.valueOf(purchase.getQuantity()), false),
                createSummaryRow("Total Paid:", totalPaid, true)
        );
        summaryBox.getStyleClass().add("ticket-summary-box");

        VBox right = new VBox(18, qrBox, summaryBox);
        right.setPrefWidth(280);
        return right;
    }

    private VBox createLabeledValue(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("success-field-label");

        Label value = new Label(valueText);
        value.getStyleClass().add("success-field-value");
        value.setWrapText(true);

        return new VBox(6, label, value);
    }

    private HBox createIconRow(String iconText, String valueText) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("success-row-icon");

        Label value = new Label(valueText);
        value.getStyleClass().add("success-row-text");
        value.setWrapText(true);

        HBox row = new HBox(10, icon, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox createSummaryRow(String labelText, String valueText, boolean highlightValue) {
        Label label = new Label(labelText);
        label.getStyleClass().add("summary-label");

        Label value = new Label(valueText);
        value.getStyleClass().add(highlightValue ? "summary-value-highlight" : "summary-value");

        HBox row = new HBox(10, label, grow(), value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label buildTicketIdStrip(Ticket ticket) {
        Label ticketIdLabel = new Label("Ticket ID: " + ticket.getTicketId());
        ticketIdLabel.setAlignment(Pos.CENTER);
        ticketIdLabel.setMaxWidth(Double.MAX_VALUE);
        ticketIdLabel.getStyleClass().add("ticket-id-strip");
        return ticketIdLabel;
    }

    private Button buildSuccessBackButton() {
        Button backButton = new Button("Back to Events");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.getStyleClass().add("success-back-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));
        return backButton;
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

    private String extractDate(String startDateTime) {
        if (startDateTime == null || startDateTime.isBlank()) {
            return "-";
        }

        String normalized = startDateTime.trim().replace("T", " ");
        String[] parts = normalized.split("\\s+");

        if (parts.length >= 1) {
            return parts[0];
        }

        return normalized;
    }

    private String extractTime(String startDateTime) {
        if (startDateTime == null || startDateTime.isBlank()) {
            return "-";
        }

        String normalized = startDateTime.trim().replace("T", " ");
        String[] parts = normalized.split("\\s+");

        if (parts.length >= 2) {
            return parts[1];
        }

        return "-";
    }

    private String safeText(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
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