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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        LinkedHashMap<String, String> ticketTypePrices = ticketController.getTicketTypePricesForEvent(event);

        String defaultType = ticketTypePrices.keySet().iterator().next();
        StringProperty selectedType = new SimpleStringProperty(defaultType);
        IntegerProperty quantity = new SimpleIntegerProperty(1);

        FlowPane ticketTypeGrid = new FlowPane();
        ticketTypeGrid.setHgap(16);
        ticketTypeGrid.setVgap(16);
        ticketTypeGrid.setPrefWrapLength(1000);

        Map<String, VBox> cardMap = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : ticketTypePrices.entrySet()) {
            String typeName = entry.getKey();
            String priceLabel = entry.getValue();

            VBox typeCard = createTicketTypeCard(typeName, priceLabel, typeName.equals(defaultType));
            typeCard.setOnMouseClicked(e -> selectTicketType(selectedType, typeName, typeCard, cardMap));

            cardMap.put(typeName, typeCard);
            ticketTypeGrid.getChildren().add(typeCard);
        }

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
                formatPrice(parsePriceValue(ticketTypePrices.get(selectedType.get())) * quantity.get()),
                "page-title"
        );

        Runnable refreshTotal = () -> totalLabel.setText(
                formatPrice(parsePriceValue(ticketTypePrices.get(selectedType.get())) * quantity.get())
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
                        quantity.get(),
                        ticketTypePrices
                )
        );

        Label helperText = styleLabel(
                "Ticket types below show only the normal ticket types configured for this event.",
                "card-text"
        );

        formCard.getChildren().addAll(
                createField("Full Name", nameField),
                createField("Email", emailField),
                styleLabel("Select Ticket Type", "notes-head"),
                helperText,
                ticketTypeGrid,
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

    private void handlePurchase(String customerName,
                                String customerEmail,
                                String ticketType,
                                int quantity,
                                LinkedHashMap<String, String> ticketTypePrices) {
        String validation = validatePurchaseInput(customerName, customerEmail, ticketType, quantity, ticketTypePrices);
        if (validation != null) {
            AlertHelper.showError("Invalid Purchase", validation);
            return;
        }

        String pricePerTicket = ticketTypePrices.get(ticketType);
        double totalValue = parsePriceValue(pricePerTicket) * quantity;

        TicketPurchase purchase = new TicketPurchase(
                event,
                customerName,
                customerEmail,
                ticketType,
                quantity,
                totalValue
        );

        Customer customer = new Customer(
                "CUST-" + UUID.randomUUID().toString().substring(0, 8),
                purchase.getCustomerName(),
                purchase.getCustomerEmail()
        );

        List<Ticket> tickets = ticketController.createEventTickets(
                event,
                customer,
                purchase.getTicketType(),
                describeTicketType(purchase.getTicketType()),
                pricePerTicket,
                event.getEndDateTime(),
                event.getLocationGuidance(),
                purchase.getQuantity()
        );

        showSuccess(tickets, purchase, formatPrice(purchase.getTotalPrice()));
    }

    private String validatePurchaseInput(String customerName,
                                         String customerEmail,
                                         String ticketType,
                                         int quantity,
                                         LinkedHashMap<String, String> ticketTypePrices) {
        StringBuilder message = new StringBuilder();

        if (customerName == null || customerName.isBlank()) {
            message.append("- Customer name is required.\n");
        }

        if (customerEmail == null || customerEmail.isBlank()) {
            message.append("- Email is required.\n");
        } else if (!customerEmail.contains("@") || !customerEmail.contains(".")) {
            message.append("- Email must be valid.\n");
        }

        if (ticketType == null || ticketType.isBlank() || !ticketTypePrices.containsKey(ticketType)) {
            message.append("- Please select a valid ticket type.\n");
        }

        if (quantity < 1) {
            message.append("- Quantity must be at least 1.\n");
        }

        return message.isEmpty() ? null : message.toString().trim();
    }

    private void showSuccess(List<Ticket> tickets, TicketPurchase purchase, String totalPaid) {
        if (tickets == null || tickets.isEmpty()) {
            AlertHelper.showError("Ticket Error", "No tickets were created.");
            return;
        }

        Ticket firstTicket = tickets.get(0);

        VBox page = new VBox();
        page.setAlignment(Pos.TOP_CENTER);
        page.setPadding(new Insets(14));
        page.getStyleClass().add("main-bg");

        VBox card = new VBox(20);
        card.setMaxWidth(760);
        card.setPadding(new Insets(24));
        card.getStyleClass().addAll("event-card", "purchase-success-card");

        card.getChildren().addAll(
                buildSuccessHeader(tickets.size()),
                new Separator(),
                buildSuccessBody(firstTicket, purchase, totalPaid),
                buildTicketCountStrip(tickets.size()),
                buildTicketIdList(tickets),
                buildSuccessBackButton()
        );

        page.getChildren().add(card);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        mainView.setContent(scrollPane);
    }

    private VBox buildSuccessHeader(int ticketCount) {
        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().add("success-icon-circle");
        iconCircle.setPrefSize(64, 64);

        Label icon = new Label("✓");
        icon.getStyleClass().add("success-icon");
        iconCircle.getChildren().add(icon);

        Label title = new Label("Purchase Successful!");
        title.getStyleClass().add("success-title");

        String subtitleText = ticketCount == 1
                ? "Your ticket has been generated"
                : ticketCount + " tickets have been generated";

        Label subtitle = new Label(subtitleText);
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

        VBox eventInfo = new VBox(12);
        eventInfo.getChildren().addAll(
                createLabeledValue("Event Name", safeText(ticket.getEventTitle())),
                createIconRow("📅", extractDate(ticket.getEventStartDateTime())),
                createIconRow("🕒", extractTime(ticket.getEventStartDateTime())),
                createIconRow("📍", safeText(ticket.getEventLocation()))
        );

        if (ticket.getEventNotes() != null && !ticket.getEventNotes().isBlank()) {
            eventInfo.getChildren().add(createLabeledValue("Notes", safeText(ticket.getEventNotes())));
        }

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

        ImageView barcodeView = new ImageView(new Image(new ByteArrayInputStream(ticket.getBarcodeImage())));
        barcodeView.setFitWidth(260);
        barcodeView.setFitHeight(80);
        barcodeView.setPreserveRatio(true);

        VBox qrBox = new VBox(10);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.getStyleClass().add("ticket-qr-box");

        Label qrHint = new Label("Scan this QR code at the venue");
        qrHint.getStyleClass().add("qr-hint");

        qrBox.getChildren().addAll(qrView, qrHint, barcodeView);

        VBox summaryBox = new VBox(
                14,
                createSummaryRow("Ticket Type:", purchase.getTicketType(), false),
                createSummaryRow("Quantity:", String.valueOf(purchase.getQuantity()), false),
                createSummaryRow("Total Paid:", totalPaid, true)
        );
        summaryBox.getStyleClass().add("ticket-summary-box");

        VBox right = new VBox(18, qrBox, summaryBox);
        right.setPrefWidth(280);
        return right;
    }

    private Label buildTicketCountStrip(int ticketCount) {
        String text = ticketCount == 1
                ? "1 ticket created"
                : ticketCount + " tickets created";

        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add("ticket-id-strip");
        return label;
    }

    private VBox buildTicketIdList(List<Ticket> tickets) {
        VBox ticketListBox = new VBox(8);

        Label heading = new Label("Generated Ticket IDs");
        heading.getStyleClass().add("success-section-title");

        ticketListBox.getChildren().add(heading);

        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);

            Label ticketIdLabel = new Label("Ticket " + (i + 1) + ": " + ticket.getTicketId());
            ticketIdLabel.setWrapText(true);
            ticketIdLabel.getStyleClass().add("ticket-id-strip");

            ticketListBox.getChildren().add(ticketIdLabel);
        }

        return ticketListBox;
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

    private Button buildSuccessBackButton() {
        Button backButton = new Button("Back to Events");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.getStyleClass().add("success-back-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));
        return backButton;
    }

    private VBox createTicketTypeCard(String title, String price, boolean selected) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18));
        card.getStyleClass().add(selected ? "ticket-type-selected" : "ticket-type");
        card.setPrefWidth(240);

        card.getChildren().addAll(
                styleLabel(title, "card-title"),
                styleLabel(price, "card-text")
        );

        return card;
    }

    private void selectTicketType(StringProperty selectedType,
                                  String nextType,
                                  VBox selectedCard,
                                  Map<String, VBox> cardMap) {
        selectedType.set(nextType);

        for (VBox card : cardMap.values()) {
            card.getStyleClass().setAll("ticket-type");
        }

        selectedCard.getStyleClass().setAll("ticket-type-selected");
    }

    private String describeTicketType(String ticketType) {
        if (ticketType == null || ticketType.isBlank()) {
            return "Ticket access";
        }

        return switch (ticketType.trim().toUpperCase(Locale.ENGLISH)) {
            case "VIP" -> "VIP access";
            case "STUDENT" -> "Student access";
            case "STANDARD" -> "Standard access";
            default -> ticketType.trim();
        };
    }

    private double parsePriceValue(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank() || "Free".equalsIgnoreCase(rawPrice.trim())) {
            return 0;
        }

        String cleaned = rawPrice
                .replace("DKK", "")
                .replace("dkk", "")
                .replace(",", ".")
                .trim();

        if (cleaned.isBlank()) {
            return 0;
        }

        return Double.parseDouble(cleaned);
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
