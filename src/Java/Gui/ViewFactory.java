package Java.Gui;

import Java.Be.Event;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ViewFactory {

    public static VBox createHeader(String appTitle, String appSubtitle, String pageTitle, String pageSubtitle) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Label l1 = new Label("SEA");
        l1.getStyleClass().add("logo-large");

        Label l2 = new Label("Erhvervsakademi");
        l2.getStyleClass().add("logo-small");

        Label l3 = new Label(appTitle);
        l3.getStyleClass().add("app-title");

        box.getChildren().addAll(l1, l2, l3);

        if (!appSubtitle.isEmpty()) {
            Label l4 = new Label(appSubtitle);
            l4.getStyleClass().add("app-subtitle");
            box.getChildren().add(l4);
        }

        if (!pageTitle.isEmpty()) {
            VBox spacing = new VBox();
            spacing.setMinHeight(20);

            Label pt = new Label(pageTitle);
            pt.getStyleClass().add("page-title-center");

            Label ps = new Label(pageSubtitle);
            ps.getStyleClass().add("page-subtitle-center");

            box.getChildren().addAll(spacing, pt, ps);
        }
        return box;
    }

    public static VBox createPortalCard(String icon, String title, String subtitle) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("portal-card");

        Label ic = new Label(icon);
        ic.getStyleClass().add("portal-icon");

        Label tL = new Label(title);
        tL.getStyleClass().add("card-title");

        Label sL = new Label(subtitle);
        sL.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(ic, tL, sL);
        return card;
    }

    public static HBox createTabs(EASVTicketsApp.TabItem... tabs) {
        HBox tabBar = new HBox(30);
        tabBar.setAlignment(Pos.CENTER_LEFT);
        tabBar.setPadding(new Insets(0, 0, 10, 0));
        tabBar.setBorder(new Border(new BorderStroke(
                null, null, null,
                new BorderWidths(0, 0, 1, 0)
        )));

        for (EASVTicketsApp.TabItem item : tabs) {
            Button tab = new Button(item.title());
            tab.getStyleClass().add(item.active() ? "tab-btn-active" : "tab-btn");
            tab.setOnAction(e -> item.action().run());
            tabBar.getChildren().add(tab);
        }

        return tabBar;
    }

    public static VBox createCoordinatorCard(String name,
                                             String email,
                                             String phone,
                                             String role,
                                             String status,
                                             String buttonText,
                                             Runnable action) {
        VBox card = new VBox(16);
        card.setPrefWidth(420);
        card.setPadding(new Insets(26));
        card.getStyleClass().add("event-card");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLbl = new Label(status);
        statusLbl.getStyleClass().add("status-active");

        top.getChildren().addAll(nameLbl, spacer, statusLbl);

        Label emailLbl = new Label(email);
        emailLbl.getStyleClass().add("card-text");

        Label phoneLbl = new Label(phone);
        phoneLbl.getStyleClass().add("card-text");

        Label roleLbl = new Label(role);
        roleLbl.getStyleClass().add("card-text");

        Button deleteBtn = new Button("\uD83D\uDDD1  " + buttonText);
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        if (action != null) {
            deleteBtn.setOnAction(e -> action.run());
        }

        card.getChildren().addAll(top, emailLbl, phoneLbl, roleLbl, deleteBtn);
        return card;
    }

    public static VBox createAccessCard(Event ev, List<String> coordinators) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("event-card");

        Label title = new Label(ev.getTitle());
        title.getStyleClass().add("card-title");

        Label date = new Label(String.valueOf(ev.getDate()));
        date.getStyleClass().add("card-text");

        Label assigned = new Label("Assigned Coordinators:");
        assigned.getStyleClass().add("notes-head");

        FlowPane pills = new FlowPane(10, 10);
        for (int i = 0; i < coordinators.size(); i++) {
            Button pill = new Button(coordinators.get(i));
            pill.getStyleClass().add(i < 2 ? "coord-pill-active" : "coord-pill");
            pill.setOnAction(e -> toggleCoordinatorPill(pill));
            pills.getChildren().add(pill);
        }

        card.getChildren().addAll(title, date, assigned, pills);
        return card;
    }

    public static VBox createCoordinatorEventCard(Event ev, Runnable onSell, Runnable onDelete) {
        VBox card = new VBox(14);
        card.setPrefWidth(420);
        card.setPrefHeight(420);
        card.setPadding(new Insets(26));
        card.getStyleClass().add("event-card");

        Label title = new Label(ev.getTitle());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        VBox topSection = new VBox(10);

        if ("Selling Fast".equalsIgnoreCase(safe(ev.getStatus()))) {
            Label status = new Label("Selling Fast");
            status.getStyleClass().add("status-fast");
            topSection.getChildren().addAll(title, status);
        } else {
            topSection.getChildren().add(title);
        }

        Label date = new Label("\uD83D\uDD52  " + safe(ev.getDate()));
        date.getStyleClass().add("card-text");
        date.setWrapText(true);

        Label venue = new Label("\uD83D\uDCCD  " + safe(ev.getLocation()));
        venue.getStyleClass().add("card-text");
        venue.setWrapText(true);

        Label capacity = new Label("\uD83D\uDC65  Available tickets");
        capacity.getStyleClass().add("card-text");

        Label notes = new Label(safe(ev.getNotes()));
        notes.getStyleClass().add("card-text");
        notes.setWrapText(true);

        Label price = new Label(safe(ev.getPrice()));
        price.getStyleClass().add("price-text");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button sellBtn = new Button("Sell Tickets");
        sellBtn.getStyleClass().add("danger-btn");
        sellBtn.setMaxWidth(Double.MAX_VALUE);
        sellBtn.setOnAction(e -> onSell.run());

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("secondary-btn");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> onDelete.run());

        card.getChildren().addAll(
                topSection,
                date,
                venue,
                capacity,
                notes,
                price,
                spacer,
                sellBtn,
                deleteBtn
        );

        return card;
    }

    public static EventForm createEventForm(String heading, Runnable saveAction, Runnable backAction, Runnable cancelAction) {
        VBox box = new VBox(18);
        box.setPadding(new Insets(30));
        box.getStyleClass().add("event-card");

        HBox top = new HBox();
        Label headingLbl = new Label(heading);
        headingLbl.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("\u2190  Back to Events");
        backBtn.getStyleClass().add("primary-btn");
        backBtn.setOnAction(e -> backAction.run());

        top.getChildren().addAll(headingLbl, spacer, backBtn);

        TextField titleField = new TextField();
        titleField.setPromptText("Enter event title");
        titleField.getStyleClass().add("input-field");

        TextField dateField = new TextField();
        dateField.setPromptText("dd/mm/yyyy");
        dateField.getStyleClass().add("input-field");

        TextField timeField = new TextField();
        timeField.setPromptText("--:--");
        timeField.getStyleClass().add("input-field");

        HBox row1 = createTwoColRow(
                createFieldBox("Event Date *", dateField),
                createFieldBox("Event Time *", timeField)
        );

        TextField venueField = new TextField();
        venueField.setPromptText("Enter venue location");
        venueField.getStyleClass().add("input-field");

        TextField capacityField = new TextField();
        capacityField.setPromptText("e.g., 300");
        capacityField.getStyleClass().add("input-field");

        TextField priceField = new TextField();
        priceField.setPromptText("e.g., 150 (or 0 for free)");
        priceField.getStyleClass().add("input-field");

        HBox row2 = createTwoColRow(
                createFieldBox("Capacity *", capacityField),
                createFieldBox("Price (DKK) *", priceField)
        );

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Add event description or notes");
        notesArea.getStyleClass().add("input-field");
        notesArea.setPrefRowCount(5);

        HBox actions = new HBox(14);
        Button createBtn = new Button("Create Event");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setOnAction(e -> saveAction.run());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");
        cancelBtn.setOnAction(e -> cancelAction.run());

        actions.getChildren().addAll(createBtn, cancelBtn);

        box.getChildren().addAll(
                top,
                createFieldBox("Event Title *", titleField),
                row1,
                createFieldBox("Venue *", venueField),
                row2,
                createFieldBox("Notes", notesArea),
                actions
        );

        return new EventForm(
                box,
                titleField,
                dateField,
                timeField,
                venueField,
                capacityField,
                priceField,
                notesArea
        );
    }

    public static CoordinatorForm createCoordinatorForm(String heading, Runnable saveAction, Runnable cancelAction) {
        VBox box = new VBox(18);
        box.setPadding(new Insets(30));
        box.getStyleClass().add("event-card");

        Label headingLbl = new Label(heading);
        headingLbl.getStyleClass().add("card-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");
        nameField.getStyleClass().add("input-field");

        TextField emailField = new TextField();
        emailField.setPromptText("email@easv.dk");
        emailField.getStyleClass().add("input-field");

        TextField phoneField = new TextField();
        phoneField.setPromptText("+45 12 34 56 78");
        phoneField.getStyleClass().add("input-field");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Event Coordinator");
        roleBox.setValue("Event Coordinator");
        roleBox.getStyleClass().add("input-field");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        HBox actions = new HBox(14);

        Button createBtn = new Button("Create Coordinator");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setOnAction(e -> saveAction.run());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");
        cancelBtn.setOnAction(e -> cancelAction.run());

        actions.getChildren().addAll(createBtn, cancelBtn);

        box.getChildren().addAll(
                headingLbl,
                createFieldBox("Full Name *", nameField),
                createFieldBox("Email *", emailField),
                createFieldBox("Phone *", phoneField),
                createFieldBox("Role *", roleBox),
                actions
        );

        return new CoordinatorForm(
                box,
                nameField,
                emailField,
                phoneField,
                roleBox
        );
    }

    public static VBox createSellTicketEventInfo(Event ev) {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.getStyleClass().add("event-card");
        box.setMaxWidth(1350);

        Label title = new Label(ev.getTitle());
        title.getStyleClass().add("page-title");

        Label starts = new Label("\uD83D\uDD52  " + safe(ev.getDate()));
        starts.getStyleClass().add("card-text");

        Label venue = new Label("\uD83D\uDCCD  " + safe(ev.getLocation()));
        venue.getStyleClass().add("card-text");

        box.getChildren().addAll(title, starts, venue);
        return box;
    }

    private static HBox createTwoColRow(VBox left, VBox right) {
        HBox row = new HBox(20);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        left.setMaxWidth(Double.MAX_VALUE);
        right.setMaxWidth(Double.MAX_VALUE);
        row.getChildren().addAll(left, right);
        return row;
    }

    private static VBox createFieldBox(String labelText, Control field) {
        VBox box = new VBox(8);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        field.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(lbl, field);
        VBox.setVgrow(field, Priority.NEVER);
        return box;
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static TicketSaleForm createTicketSaleForm(Event event, Runnable confirmAction) {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.getStyleClass().add("event-card");
        box.setMaxWidth(1350);

        Label customerTitle = new Label("Customer Information");
        customerTitle.getStyleClass().add("card-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter customer name");
        nameField.getStyleClass().add("input-field");

        TextField emailField = new TextField();
        emailField.setPromptText("customer.email@example.com");
        emailField.getStyleClass().add("input-field");

        HBox customerRow = createTwoColRow(
                createFieldBox("Full Name *", nameField),
                createFieldBox("Email Address *", emailField)
        );

        Label ticketTitle = new Label("Select Ticket Type");
        ticketTitle.getStyleClass().add("notes-head");

        StringProperty selectedTicketType = new SimpleStringProperty("STANDARD");
        double basePrice = parseEventPrice(event);

        HBox ticketTypes = new HBox(14);
        VBox standardCard = createTicketTypeCard("Standard", formatTicketPrice(basePrice), "", true);
        VBox vipCard = createTicketTypeCard("VIP", formatTicketPrice(basePrice * 1.5), "+50%", false);
        VBox studentCard = createTicketTypeCard("Student", formatTicketPrice(basePrice * 0.7), "-30%", false);
        ticketTypes.getChildren().addAll(standardCard, vipCard, studentCard);

        Label qtyTitle = new Label("Quantity");
        qtyTitle.getStyleClass().add("notes-head");

        IntegerProperty quantity = new SimpleIntegerProperty(1);

        HBox qtyBox = new HBox(16);
        qtyBox.setAlignment(Pos.CENTER_LEFT);

        Button minus = new Button("\u2212");
        minus.getStyleClass().add("secondary-btn");
        minus.setPrefSize(48, 48);

        Label qtyLbl = new Label("1");
        qtyLbl.getStyleClass().add("card-title");

        Button plus = new Button("+");
        plus.getStyleClass().add("secondary-btn");
        plus.setPrefSize(48, 48);

        qtyBox.getChildren().addAll(minus, qtyLbl, plus);

        Label totalTitle = new Label("Total Price");
        totalTitle.getStyleClass().add("card-text");

        Region totalSpacer = new Region();
        HBox.setHgrow(totalSpacer, Priority.ALWAYS);

        Label totalLbl = new Label("150.00 DKK");
        totalLbl.getStyleClass().add("page-title");

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.getChildren().addAll(totalTitle, totalSpacer, totalLbl);

        Button confirmBtn = new Button("Confirm Purchase");
        confirmBtn.getStyleClass().add("primary-btn");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> confirmAction.run());

        qtyLbl.textProperty().bind(quantity.asString());

        minus.setOnAction(e -> {
            if (quantity.get() > 1) {
                quantity.set(quantity.get() - 1);
            }
        });

        plus.setOnAction(e -> quantity.set(quantity.get() + 1));

        standardCard.setOnMouseClicked(e -> {
            selectedTicketType.set("STANDARD");
            updateTicketTypeSelection(standardCard, vipCard, studentCard);
        });

        vipCard.setOnMouseClicked(e -> {
            selectedTicketType.set("VIP");
            updateTicketTypeSelection(vipCard, standardCard, studentCard);
        });

        studentCard.setOnMouseClicked(e -> {
            selectedTicketType.set("STUDENT");
            updateTicketTypeSelection(studentCard, standardCard, vipCard);
        });

        box.getChildren().addAll(
                customerTitle,
                customerRow,
                ticketTitle,
                ticketTypes,
                qtyTitle,
                qtyBox,
                new Separator(),
                totalRow,
                confirmBtn
        );

        return new TicketSaleForm(
                box,
                nameField,
                emailField,
                selectedTicketType,
                quantity,
                totalLbl
        );
    }

    private static double parseEventPrice(Event event) {
        if (event == null || event.getPrice() == null) {
            return 0;
        }

        String priceText = event.getPrice().trim();
        if (priceText.isEmpty() || "Free".equalsIgnoreCase(priceText)) {
            return 0;
        }

        String numeric = priceText.replace("DKK", "").trim().replace(",", ".");
        return Double.parseDouble(numeric);
    }

    private static String formatTicketPrice(double price) {
        if (price == 0) {
            return "Free";
        }

        if (price == Math.floor(price)) {
            return String.format("%.0f DKK", price);
        }

        return String.format("%.2f DKK", price);
    }

    private static VBox createTicketTypeCard(String title, String price, String extra, boolean selected) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(18));
        box.setPrefWidth(260);
        box.setPrefHeight(120);
        box.getStyleClass().add(selected ? "ticket-type-selected" : "ticket-type");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("card-title");

        Label priceLbl = new Label(price);
        priceLbl.getStyleClass().add("card-text");

        box.getChildren().addAll(titleLbl, priceLbl);

        if (!extra.isEmpty()) {
            Label extraLbl = new Label(extra);
            extraLbl.getStyleClass().add(extra.startsWith("-") ? "discount-green" : "discount-red");
            box.getChildren().add(extraLbl);
        }

        return box;
    }

    private static void updateTicketTypeSelection(VBox selectedCard, VBox... otherCards) {
        selectedCard.getStyleClass().remove("ticket-type");
        if (!selectedCard.getStyleClass().contains("ticket-type-selected")) {
            selectedCard.getStyleClass().add("ticket-type-selected");
        }

        for (VBox card : otherCards) {
            card.getStyleClass().remove("ticket-type-selected");
            if (!card.getStyleClass().contains("ticket-type")) {
                card.getStyleClass().add("ticket-type");
            }
        }
    }

    private static void toggleCoordinatorPill(Button pill) {
        if (pill.getStyleClass().contains("coord-pill-active")) {
            pill.getStyleClass().remove("coord-pill-active");
            if (!pill.getStyleClass().contains("coord-pill")) {
                pill.getStyleClass().add("coord-pill");
            }
        } else {
            pill.getStyleClass().remove("coord-pill");
            if (!pill.getStyleClass().contains("coord-pill-active")) {
                pill.getStyleClass().add("coord-pill-active");
            }
        }
    }

    public static VBox createPurchaseSuccessCard(Event ev,
                                                 String customerName,
                                                 String customerEmail,
                                                 String ticketType,
                                                 int quantity,
                                                 String totalPaid,
                                                 Runnable backAction) {
        VBox box = new VBox(18);
        box.setPadding(new Insets(20));
        box.getStyleClass().add("event-card");
        box.setMaxWidth(980);

        VBox topSection = new VBox(8);
        topSection.setAlignment(Pos.CENTER);

        Label icon = new Label("\uD83C\uDFAB");
        icon.getStyleClass().add("success-icon");

        Label title = new Label("Purchase Successful!");
        title.getStyleClass().add("page-title");
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Your ticket has been generated");
        subtitle.getStyleClass().add("card-text");

        topSection.getChildren().addAll(icon, title, subtitle);

        Separator sepTop = new Separator();

        HBox middle = new HBox(24);
        middle.setAlignment(Pos.TOP_LEFT);

        VBox left = new VBox(12);
        left.setPrefWidth(420);

        Label detailsTitle = new Label("Event Details");
        detailsTitle.getStyleClass().add("card-title");

        Label eventNameHead = new Label("Event Name");
        eventNameHead.getStyleClass().add("form-label");

        Label eventName = new Label(safe(ev.getTitle()));
        eventName.getStyleClass().add("card-title");

        Label eventDate = new Label("\uD83D\uDCC5  " + extractDatePart(safe(ev.getDate())));
        eventDate.getStyleClass().add("card-text");

        Label eventTime = new Label("\uD83D\uDD52  " + extractTimePart(safe(ev.getDate())));
        eventTime.getStyleClass().add("card-text");

        Label eventVenue = new Label("\uD83D\uDCCD  " + safe(ev.getLocation()));
        eventVenue.getStyleClass().add("card-text");

        Separator leftSep = new Separator();

        Label customerTitle = new Label("Customer Information");
        customerTitle.getStyleClass().add("card-title");

        Label customerNameLbl = new Label("\uD83D\uDC64  " + customerName);
        customerNameLbl.getStyleClass().add("card-text");

        Label customerEmailLbl = new Label("\u2709  " + customerEmail);
        customerEmailLbl.getStyleClass().add("card-text");

        left.getChildren().addAll(
                detailsTitle,
                eventNameHead,
                eventName,
                eventDate,
                eventTime,
                eventVenue,
                leftSep,
                customerTitle,
                customerNameLbl,
                customerEmailLbl
        );

        VBox right = new VBox(12);
        right.setAlignment(Pos.TOP_CENTER);

        StackPane qrBox = new StackPane();
        qrBox.setPrefSize(220, 220);
        qrBox.getStyleClass().add("qr-box");

        Label qrPlaceholder = new Label("QR");
        qrPlaceholder.getStyleClass().add("qr-placeholder");
        qrBox.getChildren().add(qrPlaceholder);

        Label qrText = new Label("Scan this QR code at the venue");
        qrText.getStyleClass().add("card-text");

        VBox summaryBox = new VBox(10);
        summaryBox.setPadding(new Insets(16));
        summaryBox.getStyleClass().add("ticket-summary-box");
        summaryBox.setPrefWidth(300);

        HBox row1 = createSummaryRow("Ticket Type:", ticketType, false);
        HBox row2 = createSummaryRow("Quantity:", String.valueOf(quantity), false);

        Separator sumSep = new Separator();

        HBox row3 = createSummaryRow("Total Paid:", totalPaid, true);

        summaryBox.getChildren().addAll(row1, row2, sumSep, row3);

        right.getChildren().addAll(qrBox, qrText, summaryBox);

        middle.getChildren().addAll(left, right);

        Label ticketId = new Label("Ticket ID: EASV-1774381176086-6FQQRATT8");
        ticketId.getStyleClass().add("ticket-id-box");
        ticketId.setMaxWidth(Double.MAX_VALUE);
        ticketId.setAlignment(Pos.CENTER);

        Button backBtn = new Button("Back to Events");
        backBtn.getStyleClass().add("primary-btn");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> backAction.run());

        box.getChildren().addAll(
                topSection,
                sepTop,
                middle,
                ticketId,
                backBtn
        );

        return box;
    }

    private static HBox createSummaryRow(String leftText, String rightText, boolean highlight) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label left = new Label(leftText);
        left.getStyleClass().add("card-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label right = new Label(rightText);
        right.getStyleClass().add(highlight ? "price-text" : "card-title");

        row.getChildren().addAll(left, spacer, right);
        return row;
    }

    private static String extractDatePart(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(" at ")) {
            return value.split(" at ")[0].trim();
        }
        if (value.contains(",")) {
            return value.split(",")[0].trim();
        }
        return value;
    }

    private static String extractTimePart(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(" at ")) {
            String[] parts = value.split(" at ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        if (value.contains(",")) {
            String[] parts = value.split(",");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return "";
    }

    public static class TicketSaleForm {
        private final VBox root;
        private final TextField nameField;
        private final TextField emailField;
        private final StringProperty selectedTicketType;
        private final IntegerProperty quantity;
        private final Label totalLabel;

        public TicketSaleForm(VBox root,
                              TextField nameField,
                              TextField emailField,
                              StringProperty selectedTicketType,
                              IntegerProperty quantity,
                              Label totalLabel) {
            this.root = root;
            this.nameField = nameField;
            this.emailField = emailField;
            this.selectedTicketType = selectedTicketType;
            this.quantity = quantity;
            this.totalLabel = totalLabel;
        }

        public VBox getRoot() {
            return root;
        }

        public TextField getNameField() {
            return nameField;
        }

        public TextField getEmailField() {
            return emailField;
        }

        public String getSelectedTicketType() {
            return selectedTicketType.get();
        }

        public StringProperty selectedTicketTypeProperty() {
            return selectedTicketType;
        }

        public int getQuantity() {
            return quantity.get();
        }

        public IntegerProperty quantityProperty() {
            return quantity;
        }

        public Label getTotalLabel() {
            return totalLabel;
        }
    }

    public static class EventForm {
        private final VBox root;
        private final TextField titleField;
        private final TextField dateField;
        private final TextField timeField;
        private final TextField venueField;
        private final TextField capacityField;
        private final TextField priceField;
        private final TextArea notesArea;

        public EventForm(VBox root,
                         TextField titleField,
                         TextField dateField,
                         TextField timeField,
                         TextField venueField,
                         TextField capacityField,
                         TextField priceField,
                         TextArea notesArea) {
            this.root = root;
            this.titleField = titleField;
            this.dateField = dateField;
            this.timeField = timeField;
            this.venueField = venueField;
            this.capacityField = capacityField;
            this.priceField = priceField;
            this.notesArea = notesArea;
        }

        public VBox getRoot() {
            return root;
        }

        public TextField getTitleField() {
            return titleField;
        }

        public TextField getDateField() {
            return dateField;
        }

        public TextField getTimeField() {
            return timeField;
        }

        public TextField getVenueField() {
            return venueField;
        }

        public TextField getCapacityField() {
            return capacityField;
        }

        public TextField getPriceField() {
            return priceField;
        }

        public TextArea getNotesArea() {
            return notesArea;
        }
    }

    public static class CoordinatorForm {
        private final VBox root;
        private final TextField nameField;
        private final TextField emailField;
        private final TextField phoneField;
        private final ComboBox<String> roleBox;

        public CoordinatorForm(VBox root,
                               TextField nameField,
                               TextField emailField,
                               TextField phoneField,
                               ComboBox<String> roleBox) {
            this.root = root;
            this.nameField = nameField;
            this.emailField = emailField;
            this.phoneField = phoneField;
            this.roleBox = roleBox;
        }

        public VBox getRoot() {
            return root;
        }

        public TextField getNameField() {
            return nameField;
        }

        public TextField getEmailField() {
            return emailField;
        }

        public TextField getPhoneField() {
            return phoneField;
        }

        public ComboBox<String> getRoleBox() {
            return roleBox;
        }
    }
}
