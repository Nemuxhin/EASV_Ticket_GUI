package Java.Gui;

import Java.Be.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

        Button deleteBtn = new Button("🗑  " + buttonText);
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        if (action != null) {
            deleteBtn.setOnAction(e -> action.run());
        }

        card.getChildren().addAll(top, emailLbl, phoneLbl, roleLbl, deleteBtn);
        return card;
    }

    public static VBox createAccessCard(Event ev, List<String> coordinators, String btnText) {
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
            Label pill = new Label(coordinators.get(i));
            pill.getStyleClass().add(i < 2 ? "coord-pill-active" : "coord-pill");
            pills.getChildren().add(pill);
        }

        Button btn = new Button(btnText);
        btn.getStyleClass().add("primary-btn");

        card.getChildren().addAll(title, date, assigned, pills, btn);
        return card;
    }

    public static VBox createCoordinatorEventCard(Event ev, Runnable onSell) {
        VBox card = new VBox(14);
        card.setPrefWidth(420);
        card.setPrefHeight(420); // same card height
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

        Label date = new Label("🕒  " + safe(ev.getDate()));
        date.getStyleClass().add("card-text");
        date.setWrapText(true);

        Label venue = new Label("📍  " + safe(ev.getLocation()));
        venue.getStyleClass().add("card-text");
        venue.setWrapText(true);

        Label capacity = new Label("👥  Available tickets");
        capacity.getStyleClass().add("card-text");

        Label notes = new Label(safe(ev.getNotes()));
        notes.getStyleClass().add("card-text");
        notes.setWrapText(true);

        Label price = new Label(safe(ev.getPrice()));
        price.getStyleClass().add("price-text");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button sellBtn = new Button("Sell Tickets");
        sellBtn.getStyleClass().add("primary-btn");
        sellBtn.setMaxWidth(Double.MAX_VALUE);
        sellBtn.setOnAction(e -> onSell.run());

        card.getChildren().addAll(
                topSection,
                date,
                venue,
                capacity,
                notes,
                price,
                spacer,
                sellBtn
        );

        return card;
    }

    public static VBox createEventForm(String heading, Runnable backAction, Runnable cancelAction) {
        VBox box = new VBox(18);
        box.setPadding(new Insets(30));
        box.getStyleClass().add("event-card");

        HBox top = new HBox();
        Label headingLbl = new Label(heading);
        headingLbl.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("←  Back to Events");
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

        return box;
    }

    public static VBox createCoordinatorForm(String heading, Runnable saveAction, Runnable cancelAction) {
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

        return box;
    }

    public static VBox createSellTicketEventInfo(Event ev) {
        VBox box = new VBox(16);
        box.setPadding(new Insets(34));
        box.getStyleClass().add("event-card");
        box.setMaxWidth(1350);

        Label title = new Label(ev.getTitle());
        title.getStyleClass().add("page-title");

        Label starts = new Label("🕒  " + safe(ev.getDate()));
        starts.getStyleClass().add("card-text");

        Label venue = new Label("📍  " + safe(ev.getLocation()));
        venue.getStyleClass().add("card-text");

        box.getChildren().addAll(title, starts, venue);
        return box;
    }

    public static VBox createTicketPurchaseBox(Runnable confirmAction) {
        VBox box = new VBox(26);
        box.setPadding(new Insets(34));
        box.getStyleClass().add("event-card");
        box.setMaxWidth(1050);

        Label title = new Label("Select Ticket Type");
        title.getStyleClass().add("card-title");

        HBox ticketTypes = new HBox(18);
        ticketTypes.getChildren().addAll(
                createTicketType("Standard", "150 DKK", true),
                createTicketType("VIP", "225 DKK", false),
                createTicketType("Student", "105 DKK", false)
        );

        Label qtyLbl = new Label("Quantity");
        qtyLbl.getStyleClass().add("notes-head");

        HBox qtyBox = new HBox(14);
        qtyBox.setAlignment(Pos.CENTER_LEFT);

        Button minus = new Button("−");
        minus.getStyleClass().add("secondary-btn");

        Label qty = new Label("1");
        qty.getStyleClass().add("card-title");

        Button plus = new Button("+");
        plus.getStyleClass().add("secondary-btn");

        qtyBox.getChildren().addAll(minus, qty, plus);

        Separator sep = new Separator();

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);

        Label totalLbl = new Label("Total Price");
        totalLbl.getStyleClass().add("card-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label total = new Label("150 DKK");
        total.getStyleClass().add("page-title");

        totalRow.getChildren().addAll(totalLbl, spacer, total);

        Button confirm = new Button("Confirm Purchase");
        confirm.getStyleClass().add("primary-btn");
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setOnAction(e -> confirmAction.run());

        box.getChildren().addAll(title, ticketTypes, qtyLbl, qtyBox, sep, totalRow, confirm);
        return box;
    }

    private static VBox createTicketType(String title, String price, boolean selected) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(22));
        box.setPrefWidth(180);
        box.getStyleClass().add(selected ? "ticket-type-selected" : "ticket-type");

        Label t = new Label(title);
        t.getStyleClass().add("card-title");

        Label p = new Label(price);
        p.getStyleClass().add("card-text");

        box.getChildren().addAll(t, p);
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
    public static VBox createTicketSaleForm(Runnable confirmAction) {
        VBox box = new VBox(26);
        box.setPadding(new Insets(34));
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

        HBox ticketTypes = new HBox(20);
        ticketTypes.getChildren().addAll(
                createTicketTypeCard("Standard", "150 DKK", "", true),
                createTicketTypeCard("VIP", "225 DKK", "+50%", false),
                createTicketTypeCard("Student", "105 DKK", "-30%", false)
        );

        Label qtyTitle = new Label("Quantity");
        qtyTitle.getStyleClass().add("notes-head");

        HBox qtyBox = new HBox(22);
        qtyBox.setAlignment(Pos.CENTER_LEFT);

        Button minus = new Button("−");
        minus.getStyleClass().add("secondary-btn");
        minus.setPrefSize(64, 64);

        Label qtyLbl = new Label("1");
        qtyLbl.getStyleClass().add("card-title");

        Button plus = new Button("+");
        plus.getStyleClass().add("secondary-btn");
        plus.setPrefSize(64, 64);

        qtyBox.getChildren().addAll(minus, qtyLbl, plus);

        Button confirmBtn = new Button("Confirm Purchase");
        confirmBtn.getStyleClass().add("primary-btn");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> confirmAction.run());

        box.getChildren().addAll(
                customerTitle,
                customerRow,
                ticketTitle,
                ticketTypes,
                qtyTitle,
                qtyBox,
                confirmBtn
        );

        return box;
    }
    private static VBox createTicketTypeCard(String title, String price, String extra, boolean selected) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(28));
        box.setPrefWidth(330);
        box.setPrefHeight(170);
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
    public static VBox createPurchaseSuccessCard(Event ev,
                                                 String customerName,
                                                 String customerEmail,
                                                 String ticketType,
                                                 int quantity,
                                                 String totalPaid,
                                                 Runnable backAction) {
        VBox box = new VBox(28);
        box.setPadding(new Insets(34));
        box.getStyleClass().add("event-card");
        box.setMaxWidth(1350);

        VBox topSection = new VBox(12);
        topSection.setAlignment(Pos.CENTER);

        Label icon = new Label("🎫");
        icon.getStyleClass().add("success-icon");

        Label title = new Label("Purchase Successful!");
        title.getStyleClass().add("page-title");
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Your ticket has been generated");
        subtitle.getStyleClass().add("card-text");

        topSection.getChildren().addAll(icon, title, subtitle);

        Separator sepTop = new Separator();

        HBox middle = new HBox(40);
        middle.setAlignment(Pos.TOP_LEFT);

        VBox left = new VBox(18);
        left.setPrefWidth(620);

        Label detailsTitle = new Label("Event Details");
        detailsTitle.getStyleClass().add("card-title");

        Label eventNameHead = new Label("Event Name");
        eventNameHead.getStyleClass().add("notes-head");

        Label eventName = new Label(safe(ev.getTitle()));
        eventName.getStyleClass().add("card-title");

        Label eventDate = new Label("🗓  " + extractDatePart(safe(ev.getDate())));
        eventDate.getStyleClass().add("card-text");

        Label eventTime = new Label("🕒  " + extractTimePart(safe(ev.getDate())));
        eventTime.getStyleClass().add("card-text");

        Label eventVenue = new Label("📍  " + safe(ev.getLocation()));
        eventVenue.getStyleClass().add("card-text");

        Separator leftSep = new Separator();

        Label customerTitle = new Label("Customer Information");
        customerTitle.getStyleClass().add("card-title");

        Label customerNameLbl = new Label("👤  " + customerName);
        customerNameLbl.getStyleClass().add("card-text");

        Label customerEmailLbl = new Label("✉  " + customerEmail);
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

        VBox right = new VBox(18);
        right.setAlignment(Pos.TOP_CENTER);

        StackPane qrBox = new StackPane();
        qrBox.setPrefSize(360, 360);
        qrBox.getStyleClass().add("qr-box");

        Label qrPlaceholder = new Label("QR");
        qrPlaceholder.getStyleClass().add("qr-placeholder");
        qrBox.getChildren().add(qrPlaceholder);

        Label qrText = new Label("Scan this QR code at the venue");
        qrText.getStyleClass().add("card-text");

        VBox summaryBox = new VBox(14);
        summaryBox.setPadding(new Insets(24));
        summaryBox.getStyleClass().add("ticket-summary-box");
        summaryBox.setPrefWidth(420);

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
        if (value.contains(",")) {
            return value.split(",")[0].trim();
        }
        return value;
    }

    private static String extractTimePart(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",")) {
            String[] parts = value.split(",");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return "";
    }
}