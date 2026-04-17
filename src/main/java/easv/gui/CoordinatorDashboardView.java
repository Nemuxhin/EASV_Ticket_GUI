package easv.gui;

import easv.be.Event;
import easv.be.SoldTicketRecord;
import easv.be.Ticket;
import easv.be.User;
import easv.bll.MailClientService;
import easv.bll.QrScannerService;
import easv.bll.TicketPdfService;
import easv.bll.TicketRedemptionService;
import easv.bll.TicketScanResult;
import easv.controller.EventController;
import easv.controller.TicketController;
import easv.controller.UserController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

public class CoordinatorDashboardView {
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORM_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MainView mainView;
    private final EventController eventController;
    private final UserController userController;
    private final TicketController ticketController;
    private final String activeTab;
    private final QrScannerService qrScannerService;
    private final TicketRedemptionService ticketRedemptionService;
    private final TicketPdfService ticketPdfService;
    private final MailClientService mailClientService;

    public CoordinatorDashboardView(MainView mainView,
                                    EventController eventController,
                                    UserController userController,
                                    String activeTab) {
        this.mainView = mainView;
        this.eventController = eventController;
        this.userController = userController;
        this.ticketController = new TicketController();
        this.activeTab = activeTab;
        this.qrScannerService = new QrScannerService();
        this.ticketRedemptionService = new TicketRedemptionService();
        this.ticketPdfService = new TicketPdfService();
        this.mailClientService = new MailClientService();
    }

    public Parent getView() {
        javafx.scene.layout.BorderPane layout = new javafx.scene.layout.BorderPane();
        layout.getStyleClass().add("main-bg");

        VBox shell = new VBox();
        shell.getChildren().addAll(createTopHeader(), createTopNavigation());

        VBox content = switch (activeTab) {
            case "Manage Access" -> createManageAccessContent();
            case "Sold Tickets" -> createSoldTicketsContent();
            case "Special Tickets" -> createSpecialTicketsContent();
            case "Edit Event" -> createEditEventContent();
            case "Create Event" -> createCreateEventContent();
            default -> createEventsContent();
        };

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("dashboard-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        shell.getChildren().add(scrollPane);
        layout.setCenter(shell);

        return layout;
    }

    private HBox createTopHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("portal-topbar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 22, 12, 22));

        Label brand = new Label("SEA");
        brand.getStyleClass().add("portal-topbar-brand");

        Label school = new Label("Erhvervsakademi");
        school.getStyleClass().add("portal-topbar-school");

        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        separator.getStyleClass().add("portal-topbar-separator");

        Label title = new Label("EASV Tickets - Event Coordinator Portal");
        title.getStyleClass().add("portal-topbar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("\u2190 Back to Portal Selection");
        backBtn.getStyleClass().add("portal-topbar-back-btn");
        backBtn.setOnAction(e -> mainView.showPortalSelection());

        header.getChildren().addAll(brand, school, separator, title, spacer, backBtn);
        return header;
    }

    private VBox createTopNavigation() {
        VBox wrapper = new VBox(0);
        wrapper.setPadding(new Insets(22, 34, 0, 34));

        HBox nav = new HBox(28);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.getStyleClass().add("portal-tab-row");

        nav.getChildren().addAll(
                createTopNavButton(
                        "Events",
                        "Events".equals(activeTab) || "Edit Event".equals(activeTab),
                        "Events"
                ),
                createTopNavButton(
                        "Create Event",
                        "Create Event".equals(activeTab),
                        "Create Event"
                ),
                createTopNavButton(
                        "Manage Access",
                        "Manage Access".equals(activeTab),
                        "Manage Access"
                ),
                createTopNavButton(
                        "Special Tickets",
                        "Special Tickets".equals(activeTab),
                        "Special Tickets"
                ),
                createTopNavButton(
                        "Sold Tickets",
                        "Sold Tickets".equals(activeTab),
                        "Sold Tickets"
                )
        );

        Separator divider = new Separator();
        divider.getStyleClass().add("portal-tab-divider");

        wrapper.getChildren().addAll(nav, divider);
        return wrapper;
    }

    private Button createTopNavButton(String label, boolean active, String targetTab) {
        Button button = new Button(label);
        button.getStyleClass().add(active ? "portal-tab-btn-active" : "portal-tab-btn");
        button.setOnAction(e -> mainView.showCoordinatorDashboard(targetTab));
        return button;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20));

        Label logo = new Label("Coordinator Portal");
        logo.getStyleClass().add("sidebar-logo");

        Button eventsBtn = createMenuBtn(
                "\uD83D\uDCC5 Events",
                "Events".equals(activeTab) || "Edit Event".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Events")
        );

        Button createEventBtn = createMenuBtn(
                "+ Create Event",
                "Create Event".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Create Event")
        );

        Button accessBtn = createMenuBtn(
                "\uD83D\uDC65 Manage Access",
                "Manage Access".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Manage Access")
        );

        Button specialTicketsBtn = createMenuBtn(
                "\u2726 Special Tickets",
                "Special Tickets".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Special Tickets")
        );

        Button soldBtn = createMenuBtn(
                "\uD83C\uDFAB Sold Tickets",
                "Sold Tickets".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Sold Tickets")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("\uD83D\uDEAA Logout");
        logoutBtn.getStyleClass().add("sidebar-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> mainView.showPortalSelection());

        sidebar.getChildren().addAll(
                logo,
                eventsBtn,
                createEventBtn,
                accessBtn,
                specialTicketsBtn,
                soldBtn,
                spacer,
                logoutBtn
        );
        return sidebar;
    }

    private VBox createEventsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 34, 34, 34));
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events by title or venue...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(Double.MAX_VALUE);

        HBox toolbar = new HBox(searchBar);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBar, Priority.ALWAYS);

        GridPane grid = createTwoColumnGrid();
        List<Event> visibleEvents = getVisibleCoordinatorEvents();

        Runnable render = () -> {
            List<Event> filteredEvents = new ArrayList<>();
            for (Event event : visibleEvents) {
                if (matchesSearch(event, searchBar.getText())) {
                    filteredEvents.add(event);
                }
            }
            renderCoordinatorEventCards(grid, filteredEvents);
        };

        searchBar.textProperty().addListener((obs, oldValue, newValue) -> render.run());
        render.run();

        content.getChildren().addAll(title, toolbar, grid);
        return content;
    }

    private VBox createManageAccessContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(30, 34, 34, 34));

        Label title = new Label("Manage Coordinator Access");
        title.getStyleClass().add("page-title");

        VBox list = new VBox(16);
        List<User> coordinators = userController.getUsersByRole("Event Coordinator");
        List<Event> visibleEvents = getVisibleCoordinatorEvents();

        for (Event event : visibleEvents) {
            list.getChildren().add(createAccessCard(event, coordinators));
        }

        content.getChildren().addAll(title, list);
        return content;
    }

    private VBox createSoldTicketsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 34, 34, 34));

        Label title = new Label("Sold Tickets");
        title.getStyleClass().add("page-title");

        Label count = new Label();
        count.getStyleClass().add("card-text");

        HBox top = new HBox(title, grow(), count);
        top.setAlignment(Pos.CENTER_LEFT);

        Label statusSummary = new Label();
        statusSummary.getStyleClass().add("card-text");

        Label redeemTitle = new Label("Redeem Ticket");
        redeemTitle.getStyleClass().add("card-title");

        Label redeemHelp = new Label(
                "Scan a QR image, choose a ticket PDF, or paste the public code / ticket ID to redeem it. Successful scans mark the ticket as used once."
        );
        redeemHelp.getStyleClass().add("card-text");
        redeemHelp.setWrapText(true);

        TextField tokenField = new TextField();
        tokenField.setPromptText("Scan or paste the public code or ticket ID here");
        tokenField.getStyleClass().add("input-field");

        Label redeemStatus = new Label("Ready");
        redeemStatus.getStyleClass().add("status-avail");

        TextArea redeemResultArea = new TextArea("No ticket scanned yet.");
        redeemResultArea.setEditable(false);
        redeemResultArea.setWrapText(true);
        redeemResultArea.setPrefRowCount(7);
        redeemResultArea.getStyleClass().add("input-field");

        Button chooseImageButton = new Button("Choose QR Image");
        chooseImageButton.getStyleClass().add("secondary-btn");

        Button choosePdfButton = new Button("Choose Ticket PDF");
        choosePdfButton.getStyleClass().add("secondary-btn");

        Button redeemButton = new Button("Redeem Ticket");
        redeemButton.getStyleClass().add("primary-btn");

        Button clearButton = new Button("Clear");
        clearButton.getStyleClass().add("secondary-btn");

        HBox redeemActions = new HBox(10, chooseImageButton, choosePdfButton, redeemButton, clearButton);
        redeemActions.setAlignment(Pos.CENTER_LEFT);

        VBox redeemCard = new VBox(12, redeemTitle, redeemHelp, tokenField, redeemActions, redeemStatus, redeemResultArea);
        redeemCard.getStyleClass().addAll("event-card", "event-list-card");
        redeemCard.setPadding(new Insets(20));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by public code, ticket ID, email, customer, event, or ticket type...");
        searchField.getStyleClass().add("search-bar");
        searchField.setPrefWidth(580);

        TextField onsiteEmailField = new TextField();
        onsiteEmailField.setPromptText("Filter sold tickets by email");
        onsiteEmailField.getStyleClass().add("input-field");
        onsiteEmailField.setPrefWidth(260);

        Button emailLookupBtn = new Button("Find By Email");
        emailLookupBtn.getStyleClass().add("secondary-btn");
        emailLookupBtn.setOnAction(e -> {
            String email = onsiteEmailField.getText().trim();
            if (!isValidEmail(email)) {
                AlertHelper.showError("Invalid Email", "Please enter a valid email address.");
                return;
            }
            searchField.setText(email);
        });

        HBox searchRow = new HBox(10, searchField, onsiteEmailField, emailLookupBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        VBox bodyCard = new VBox(14);
        bodyCard.getStyleClass().addAll("event-card", "event-list-card");
        bodyCard.setPadding(new Insets(26));

        VBox list = new VBox(10);
        bodyCard.getChildren().add(list);

        final Runnable[] refreshList = new Runnable[1];
        refreshList[0] = () -> renderSoldTicketsList(
                list,
                searchField.getText(),
                count,
                statusSummary,
                redeemStatus,
                redeemResultArea,
                refreshList[0]
        );

        chooseImageButton.setOnAction(e ->
                chooseTicketImageAndRedeem(tokenField, redeemStatus, redeemResultArea, refreshList[0])
        );

        choosePdfButton.setOnAction(e ->
                chooseTicketPdfAndRedeem(tokenField, redeemStatus, redeemResultArea, refreshList[0])
        );

        redeemButton.setOnAction(e ->
                redeemTicketFromInput(tokenField, redeemStatus, redeemResultArea, refreshList[0])
        );

        clearButton.setOnAction(e -> {
            tokenField.clear();
            updateRedeemFeedback(redeemStatus, redeemResultArea, true, "Ready", "No ticket scanned yet.");
        });

        tokenField.setOnAction(e ->
                redeemTicketFromInput(tokenField, redeemStatus, redeemResultArea, refreshList[0])
        );

        searchField.textProperty().addListener((obs, oldValue, newValue) -> refreshList[0].run());
        refreshList[0].run();

        content.getChildren().addAll(top, statusSummary, redeemCard, searchRow, bodyCard);
        return content;
    }

    private void renderSoldTicketsList(VBox list,
                                       String query,
                                       Label count,
                                       Label statusSummary,
                                       Label redeemStatus,
                                       TextArea redeemResultArea,
                                       Runnable refreshList) {
        String normalizedQuery = query == null ? "" : query.trim();

        List<SoldTicketRecord> soldTickets = normalizedQuery.isBlank()
                ? ticketController.getRecentSoldTickets(10)
                : ticketController.searchSoldTickets(normalizedQuery, 100);

        updateSoldTicketSummary(count, statusSummary, soldTickets, normalizedQuery.isBlank());

        list.getChildren().clear();

        if (soldTickets.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));

            Label icon = new Label("\uD83C\uDFAB");
            icon.getStyleClass().add("page-title");

            Label emptyTitle = new Label(normalizedQuery.isBlank()
                    ? "No recent tickets"
                    : "No tickets match your search");
            emptyTitle.getStyleClass().add("card-title");

            Label emptyText = new Label(normalizedQuery.isBlank()
                    ? "The latest 10 sold tickets will appear here."
                    : "Try searching by ticket ID, public code, email, customer, event, or ticket type.");
            emptyText.getStyleClass().add("card-text");

            emptyBox.getChildren().addAll(icon, emptyTitle, emptyText);
            list.getChildren().add(emptyBox);
            return;
        }

        for (SoldTicketRecord ticket : soldTickets) {
            list.getChildren().add(createSoldTicketRow(ticket, redeemStatus, redeemResultArea, refreshList));
        }
    }

    private VBox createSpecialTicketsContent() {
        VBox content = new VBox();
        content.getChildren().add(new CreateSpecialTicketView(mainView).getView());
        return content;
    }

    private VBox createSoldTicketRow(SoldTicketRecord ticket,
                                     Label redeemStatus,
                                     TextArea redeemResultArea,
                                     Runnable refreshList) {
        VBox row = new VBox(8);
        row.getStyleClass().addAll("event-card", "event-list-card");
        row.setPadding(new Insets(14));

        boolean used = ticket.isUsed();

        String customerName = displayText(ticket.getCustomerName());
        String customerEmail = displayText(ticket.getCustomerEmail());
        String customerLine = "-";
        if (!"-".equals(customerName) || !"-".equals(customerEmail)) {
            customerLine = customerName + " (" + customerEmail + ")";
        }

        String ticketIdText = displayText(ticket.getTicketId());

        Label title = new Label(displayText(ticket.getEventName()));
        title.getStyleClass().add("card-title");

        Label status = new Label(used ? "Used" : "Not Used");
        status.getStyleClass().add(used ? "status-fast" : "status-avail");

        HBox top = new HBox(10, title, grow(), status);
        top.setAlignment(Pos.CENTER_LEFT);

        Label details = new Label(
                "Type: " + displayText(ticket.getTicketType()) +
                        "  |  Price: " + displayText(ticket.getPrice())
        );
        details.getStyleClass().add("card-text");

        Label customer = new Label("Customer: " + customerLine);
        customer.getStyleClass().add("card-text");

        Label ticketId = new Label("Ticket ID: " + ticketIdText);
        ticketId.getStyleClass().add("notes-head");

        Label publicCode = new Label("Public Code: " + displayText(ticket.getPublicCode()));
        publicCode.getStyleClass().add("card-text");

        HBox actions = buildSoldTicketActions(ticket, used, redeemStatus, redeemResultArea, refreshList);

        row.getChildren().addAll(top, details, customer, ticketId, publicCode, actions);
        return row;
    }

    private HBox buildSoldTicketActions(SoldTicketRecord soldTicket,
                                        boolean used,
                                        Label redeemStatus,
                                        TextArea redeemResultArea,
                                        Runnable refreshList) {
        boolean canPrint = !safeText(soldTicket.getPublicCode()).isBlank();
        boolean canEmail = canPrint && isValidEmail(soldTicket.getCustomerEmail());
        boolean canRedeem = !used && canPrint;

        Button printPdfButton = new Button("Print PDF");
        printPdfButton.getStyleClass().add("secondary-btn");
        printPdfButton.setDisable(!canPrint);
        printPdfButton.setOnAction(e -> printSoldTicketPdf(soldTicket));

        Button emailButton = new Button("Send Email");
        emailButton.getStyleClass().add("secondary-btn");
        emailButton.setDisable(!canEmail);
        emailButton.setOnAction(e -> sendSoldTicketByEmail(soldTicket));

        Button redeemButton = new Button(used ? "Already Used" : "Redeem Ticket");
        redeemButton.getStyleClass().add(canRedeem ? "primary-btn" : "secondary-btn");
        redeemButton.setDisable(!canRedeem);
        redeemButton.setOnAction(e ->
                redeemTicketToken(displayText(soldTicket.getPublicCode()), redeemStatus, redeemResultArea, refreshList)
        );

        HBox actions = new HBox(10, printPdfButton, emailButton, redeemButton);
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private void printSoldTicketPdf(SoldTicketRecord soldTicket) {
        Ticket ticketToPrint = ticketController.buildTicketFromSoldRecord(soldTicket);
        if (ticketToPrint == null) {
            AlertHelper.showError("Print Failed", "The ticket could not be rebuilt from the sold ticket record.");
            return;
        }

        try {
            ticketPdfService.printTicketPdf(ticketToPrint);
        } catch (Exception ex) {
            AlertHelper.showError("Print Failed", ex.getMessage());
        }
    }

    private void sendSoldTicketByEmail(SoldTicketRecord soldTicket) {
        if (!isValidEmail(soldTicket.getCustomerEmail())) {
            AlertHelper.showError("Email Failed", "This sold ticket does not have a valid customer email address.");
            return;
        }

        Ticket ticketToSend = ticketController.buildTicketFromSoldRecord(soldTicket);
        if (ticketToSend == null) {
            AlertHelper.showError("Email Failed", "The ticket could not be rebuilt from the sold ticket record.");
            return;
        }

        try {
            ticketPdfService.openTicketPdf(ticketToSend);

            String subject = "Your ticket for " + displayText(soldTicket.getEventName());
            String body = buildSoldTicketEmailBody(soldTicket, ticketToSend);

            mailClientService.openDraft(soldTicket.getCustomerEmail().trim(), subject, body);
        } catch (Exception ex) {
            AlertHelper.showError("Email Failed", ex.getMessage());
        }
    }

    private String buildSoldTicketEmailBody(SoldTicketRecord soldTicket, Ticket resolvedTicket) {
        String customerName = displayText(soldTicket.getCustomerName());
        if ("-".equals(customerName)) {
            customerName = "there";
        }

        String ticketId = displayText(soldTicket.getTicketId());

        StringBuilder builder = new StringBuilder();
        builder.append("Hi ").append(customerName).append(",\n\n");
        builder.append("Your ticket for ").append(displayText(soldTicket.getEventName())).append(" is ready.\n");
        builder.append("Ticket ID: ").append(ticketId).append("\n");
        builder.append("Public code: ").append(displayText(soldTicket.getPublicCode())).append("\n");
        builder.append("Ticket type: ").append(displayText(soldTicket.getTicketType())).append("\n");
        builder.append("Price: ").append(displayText(soldTicket.getPrice())).append("\n\n");
        builder.append("The ticket PDF has been opened so you can attach it to this email draft.\n\n");
        builder.append("Best regards,\nEASV Tickets");
        return builder.toString();
    }

    private void updateSoldTicketSummary(Label count,
                                         Label statusSummary,
                                         List<SoldTicketRecord> soldTickets,
                                         boolean showingRecentOnly) {
        int usedCount = 0;
        for (SoldTicketRecord ticket : soldTickets) {
            if (ticket.isUsed()) {
                usedCount++;
            }
        }

        int unusedCount = soldTickets.size() - usedCount;

        if (showingRecentOnly) {
            count.setText("\uD83C\uDFAB Showing latest " + soldTickets.size() + " sold tickets");
            statusSummary.setText("Used: " + usedCount + "   |   Not Used: " + unusedCount + "   |   Search to find older tickets");
        } else {
            count.setText("\uD83C\uDFAB Search results: " + soldTickets.size() + " ticket" + (soldTickets.size() == 1 ? "" : "s"));
            statusSummary.setText("Used: " + usedCount + "   |   Not Used: " + unusedCount);
        }
    }

    private void chooseTicketImageAndRedeem(TextField tokenField,
                                            Label redeemStatus,
                                            TextArea redeemResultArea,
                                            Runnable refreshList) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose QR / Barcode Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File file = chooser.showOpenDialog(mainView.getRoot().getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String token = qrScannerService.decodeToken(file);
            tokenField.setText(token);
            redeemTicketToken(token, redeemStatus, redeemResultArea, refreshList);
        } catch (Exception ex) {
            updateRedeemFeedback(redeemStatus, redeemResultArea, false, "Scan Failed", ex.getMessage());
        }
    }

    private void chooseTicketPdfAndRedeem(TextField tokenField,
                                          Label redeemStatus,
                                          TextArea redeemResultArea,
                                          Runnable refreshList) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Ticket PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = chooser.showOpenDialog(mainView.getRoot().getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String token = qrScannerService.decodeTokenFromPdf(file);
            tokenField.setText(token);
            redeemTicketToken(token, redeemStatus, redeemResultArea, refreshList);
        } catch (Exception ex) {
            updateRedeemFeedback(redeemStatus, redeemResultArea, false, "Scan Failed", ex.getMessage());
        }
    }

    private void redeemTicketFromInput(TextField tokenField,
                                       Label redeemStatus,
                                       TextArea redeemResultArea,
                                       Runnable refreshList) {
        redeemTicketToken(tokenField.getText(), redeemStatus, redeemResultArea, refreshList);
    }

    private void redeemTicketToken(String token,
                                   Label redeemStatus,
                                   TextArea redeemResultArea,
                                   Runnable refreshList) {
        TicketScanResult result = ticketRedemptionService.redeem(mainView.getCurrentUser(), null, token);

        updateRedeemFeedback(
                redeemStatus,
                redeemResultArea,
                result.success(),
                result.title(),
                buildRedeemMessage(result)
        );

        if (result.success() && refreshList != null) {
            refreshList.run();
        }
    }

    private void updateRedeemFeedback(Label redeemStatus,
                                      TextArea redeemResultArea,
                                      boolean success,
                                      String statusText,
                                      String messageText) {
        redeemStatus.setText(statusText);
        redeemStatus.getStyleClass().removeAll("status-avail", "status-fast");
        redeemStatus.getStyleClass().add(success ? "status-avail" : "status-fast");
        redeemResultArea.setText(messageText);
    }

    private String buildRedeemMessage(TicketScanResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append(result.message());

        Ticket ticket = result.ticket();
        if (ticket == null) {
            return builder.toString();
        }

        builder.append("\n\nTicket ID: ").append(displayText(ticket.getTicketId()));
        builder.append("\nPublic Code: ").append(displayText(ticket.getSecureToken()));
        builder.append("\nEvent: ").append(displayText(ticket.getEventTitle()));
        builder.append("\nType: ").append(displayText(ticket.getTicketType()));
        builder.append("\nPrice: ").append(displayText(ticket.getPrice()));
        builder.append("\nUsed: ").append(ticket.isUsed() ? "Yes" : "No");
        builder.append("\nActive: ").append(ticket.isActive() ? "Yes" : "No");

        if (ticket.hasCustomer()) {
            builder.append("\nCustomer: ").append(displayText(ticket.getCustomer().getName()));
            builder.append("\nEmail: ").append(displayText(ticket.getCustomer().getEmail()));
        }

        return builder.toString();
    }

    private VBox createEventCard(Event event) {
        return createEventCard(event, true);
    }

    private VBox createEventCard(Event event, boolean allowDelete) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);

        HBox top = new HBox(12);
        top.setAlignment(Pos.TOP_LEFT);
        Label titleLbl = new Label(displayText(event.getTitle()));
        titleLbl.getStyleClass().add("card-title");
        titleLbl.setWrapText(true);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        String status = ticketController.getEventStatus(event);
        Label statusLbl = new Label(status);
        statusLbl.getStyleClass().add(statusStyleClass(status));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(titleLbl, spacer, statusLbl);

        VBox scheduleBox = new VBox(6);
        Label startLbl = new Label("\uD83D\uDD52 " + displayText(event.getStartDateTime()));
        startLbl.getStyleClass().add("card-text");
        startLbl.setWrapText(true);
        scheduleBox.getChildren().add(startLbl);

        VBox locationBox = new VBox(6);
        Label locationLbl = new Label("\uD83D\uDCCD " + displayText(event.getLocation()));
        locationLbl.getStyleClass().add("card-text");
        locationLbl.setWrapText(true);
        locationBox.getChildren().add(locationLbl);

        Label notesHead = new Label("Notes");
        notesHead.getStyleClass().add("notes-head");

        Label notesLbl = new Label(displayText(event.getNotes()));
        notesLbl.getStyleClass().add("card-text");
        notesLbl.setWrapText(true);

        Label priceLbl = new Label(displayText(event.getPrice()));
        priceLbl.getStyleClass().add("price-text");

        // (Samu) Show the ticket types directly on the event card.
        Label ticketTypesHead = new Label("Ticket Types");
        ticketTypesHead.getStyleClass().add("notes-head");
        Label ticketTypesSummary = buildTicketTypeSummaryLabel(ticketController.getTicketTypePricesForEvent(event));

        Region actionSpacer = new Region();
        VBox.setVgrow(actionSpacer, Priority.ALWAYS);

        VBox actionSection = new VBox(10);
        actionSection.setFillWidth(true);

        Button sellBtn = new Button("Sell Ticket");
        sellBtn.getStyleClass().add("primary-btn");
        sellBtn.setMaxWidth(Double.MAX_VALUE);
        sellBtn.setOnAction(e -> mainView.showTicketSales(event));

        Button directionsBtn = new Button("Directions");
        directionsBtn.getStyleClass().add("secondary-btn");
        directionsBtn.setMaxWidth(Double.MAX_VALUE);
        directionsBtn.setOnAction(e -> MapViewHelper.openDirections(event));

        Button editBtn = new Button("Edit Event");
        editBtn.getStyleClass().add("secondary-btn");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.setOnAction(e -> mainView.showEditEvent(event));

        actionSection.getChildren().addAll(
                new Separator(),
                priceLbl,
                ticketTypesHead,
                ticketTypesSummary,
                sellBtn,
                directionsBtn,
                editBtn
        );

        if (allowDelete) {
            Button deleteBtn = new Button("\uD83D\uDDD1 Delete Event");
            deleteBtn.getStyleClass().add("danger-btn");
            deleteBtn.setMaxWidth(Double.MAX_VALUE);
            deleteBtn.setOnAction(e -> {
                // (Samu) Ask for confirmation before deleting an event.
                boolean confirmed = AlertHelper.showConfirmation(
                        "Delete Event",
                        "Are you sure you want to delete the event?"
                );
                if (!confirmed) {
                    return;
                }

                eventController.deleteEvent(event);
                ticketController.removeConfiguredTicketTypes(event);
                mainView.showCoordinatorDashboard("Events");
            });

            actionSection.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(
                top,
                scheduleBox,
                locationBox,
                notesHead,
                notesLbl,
                actionSpacer,
                actionSection
        );

        return card;
    }

    private VBox createAccessCard(Event event, List<User> coordinators) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("event-card", "event-list-card");

        Label title = new Label(displayText(event.getTitle()));
        title.getStyleClass().add("card-title");

        Label date = new Label(formatCompactDateTime(displayText(event.getStartDateTime())));
        date.getStyleClass().add("card-text");

        Label assigned = new Label("Assigned Coordinators:");
        assigned.getStyleClass().add("notes-head");

        FlowPane pills = new FlowPane(8, 8);
        LinkedHashSet<String> selectedNames = new LinkedHashSet<>();

        if (event.getCoordinators() != null) {
            for (String name : event.getCoordinators()) {
                if (name != null && !name.isBlank()) {
                    selectedNames.add(name.trim());
                }
            }
        }

        for (User coordinator : coordinators) {
            String coordinatorName = coordinator.getName();
            Button pill = new Button(coordinatorName);
            pill.getStyleClass().add("assign-pill");

            if (selectedNames.contains(coordinatorName)) {
                pill.getStyleClass().add("assign-pill-selected");
            }

            pill.setOnAction(e -> {
                if (selectedNames.contains(coordinatorName)) {
                    selectedNames.remove(coordinatorName);
                    pill.getStyleClass().remove("assign-pill-selected");
                } else {
                    selectedNames.add(coordinatorName);
                    if (!pill.getStyleClass().contains("assign-pill-selected")) {
                        pill.getStyleClass().add("assign-pill-selected");
                    }
                }

                eventController.setCoordinators(event, selectedNames.toArray(new String[0]));
            });

            pills.getChildren().add(pill);
        }

        if (coordinators.isEmpty()) {
            Label none = new Label("No coordinators available yet");
            none.getStyleClass().add("card-text");
            pills.getChildren().add(none);
        }

        card.getChildren().addAll(title, date, assigned, pills);
        return card;
    }

    private Button createMenuBtn(String text,
                                 boolean isActive,
                                 javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        btn.getStyleClass().add(isActive ? "sidebar-menu-btn-active" : "sidebar-menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(action);
        return btn;
    }

    private VBox createCreateEventContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 34, 34, 34));

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        VBox card = new VBox(18);
        card.getStyleClass().addAll("event-card", "event-list-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label cardTitle = new Label("Create New Event");
        cardTitle.getStyleClass().add("card-title");

        Region cardSpacer = new Region();
        HBox.setHgrow(cardSpacer, Priority.ALWAYS);

        Button backBtn = new Button("\u2190 Back to Events");
        backBtn.getStyleClass().add("primary-btn");
        backBtn.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        top.getChildren().addAll(cardTitle, cardSpacer, backBtn);

        EventEditorForm form = new EventEditorForm();

        Label ticketTypesTitle = new Label("Ticket Types");
        ticketTypesTitle.getStyleClass().add("card-title");

        Label configureHint = new Label("Add different ticket types like VIP or Student with custom pricing.");
        configureHint.getStyleClass().add("card-text");

        Label ticketTypesSummary = buildTicketTypeSummaryLabel(form.ticketTypePrices);

        Button configureTypesBtn = new Button("\uD83C\uDFAB Add / Edit Ticket Types");
        configureTypesBtn.getStyleClass().add("primary-btn");
        configureTypesBtn.setOnAction(e -> {
            LinkedHashMap<String, String> updated =
                    showTicketTypeConfigurationDialog(form.ticketTypePrices, configureTypesBtn.getScene(), form, null);
            if (updated != null) {
                form.ticketTypePrices = updated;
                ticketTypesSummary.setText(buildTicketTypeSummaryText(updated));
                String standardPrice = updated.get("Standard");
                if (standardPrice != null) {
                    form.priceField.setText(toEditablePrice(standardPrice));
                }
            }
        });

        Button createBtn = new Button("Create Event");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setOnAction(e -> createEventFromForm(form));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");
        cancelBtn.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        HBox actions = new HBox(12, createBtn, cancelBtn);

        card.getChildren().addAll(
                top,
                form.titleBox,
                form.startDateTimeRow,
                form.endDateTimeRow,
                form.locationBox,
                form.locationGuidanceBox,
                form.extraRow,
                form.notesBox,
                new Separator(),
                ticketTypesTitle,
                configureHint,
                ticketTypesSummary,
                configureTypesBtn,
                actions
        );

        content.getChildren().addAll(title, card);
        return content;
    }

    private VBox createEditEventContent() {
        Event seedEvent = mainView.getEditingEvent();
        if (seedEvent == null) {
            return createEventsContent();
        }

        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 34, 34, 34));

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        VBox card = new VBox(18);
        card.getStyleClass().addAll("event-card", "event-list-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label cardTitle = new Label("Edit Event");
        cardTitle.getStyleClass().add("card-title");

        Region cardSpacer = new Region();
        HBox.setHgrow(cardSpacer, Priority.ALWAYS);

        Button backBtn = new Button("\u2190 Back to Events");
        backBtn.getStyleClass().add("primary-btn");
        backBtn.setOnAction(e -> {
            mainView.clearEditingEvent();
            mainView.showCoordinatorDashboard("Events");
        });

        top.getChildren().addAll(cardTitle, cardSpacer, backBtn);

        EventEditorForm form = new EventEditorForm(seedEvent);

        Label ticketTypesTitle = new Label("Ticket Types");
        ticketTypesTitle.getStyleClass().add("card-title");

        Label configureHint = new Label("Add or update ticket types like VIP or Student with custom pricing.");
        configureHint.getStyleClass().add("card-text");

        Label ticketTypesSummary = buildTicketTypeSummaryLabel(form.ticketTypePrices);

        Button configureTypesBtn = new Button("\uD83C\uDFAB Add / Edit Ticket Types");
        configureTypesBtn.getStyleClass().add("primary-btn");
        configureTypesBtn.setOnAction(e -> {
            LinkedHashMap<String, String> updated =
                    showTicketTypeConfigurationDialog(form.ticketTypePrices, configureTypesBtn.getScene(), form, seedEvent);
            if (updated != null) {
                form.ticketTypePrices = updated;
                ticketTypesSummary.setText(buildTicketTypeSummaryText(updated));
                String standardPrice = updated.get("Standard");
                if (standardPrice != null) {
                    form.priceField.setText(toEditablePrice(standardPrice));
                }
            }
        });

        Button saveBtn = new Button("Save Event");
        saveBtn.getStyleClass().add("primary-btn");
        saveBtn.setOnAction(e -> updateEventFromForm(seedEvent, form));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");
        cancelBtn.setOnAction(e -> {
            mainView.clearEditingEvent();
            mainView.showCoordinatorDashboard("Events");
        });

        HBox actions = new HBox(12, saveBtn, cancelBtn);

        card.getChildren().addAll(
                top,
                form.titleBox,
                form.startDateTimeRow,
                form.endDateTimeRow,
                form.locationBox,
                form.locationGuidanceBox,
                form.extraRow,
                form.notesBox,
                new Separator(),
                ticketTypesTitle,
                configureHint,
                ticketTypesSummary,
                configureTypesBtn,
                actions
        );

        content.getChildren().addAll(title, card);
        return content;
    }

    private LinkedHashMap<String, String> showTicketTypeConfigurationDialog(LinkedHashMap<String, String> current,
                                                                            Scene ownerScene,
                                                                            EventEditorForm form,
                                                                            Event targetEvent) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setResizable(false);

        if (ownerScene != null && ownerScene.getWindow() != null) {
            popup.initOwner(ownerScene.getWindow());
        }

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-bg");

        VBox card = new VBox(16);
        card.getStyleClass().addAll("event-card", "event-list-card");
        card.setPadding(new Insets(24));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Ticket Configuration");
        title.getStyleClass().add("page-title");

        Button backBtn = new Button("\u2190 Back to Event Form");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setOnAction(e -> popup.close());

        header.getChildren().addAll(title, grow(), backBtn);

        Label subtitle = new Label("Configure ticket types and special perks for your event");
        subtitle.getStyleClass().add("card-text");

        Label section = new Label("Multiple Ticket Types");
        section.getStyleClass().add("card-title");

        Label sectionSub = new Label("Configure different ticket types like VIP, Student, Early Bird");
        sectionSub.getStyleClass().add("card-text");

        VBox rowsBox = new VBox(10);
        List<TicketTypeRow> rows = new ArrayList<>();

        LinkedHashMap<String, String> initial = current == null ? new LinkedHashMap<>() : new LinkedHashMap<>(current);
        if (initial.isEmpty()) {
            initial.put("Standard", "Free");
        }

        for (Map.Entry<String, String> entry : initial.entrySet()) {
            TicketTypeRow row = new TicketTypeRow(entry.getKey(), toEditablePrice(entry.getValue()));
            rows.add(row);
            rowsBox.getChildren().add(row.container);
        }

        Label countLabel = new Label();
        countLabel.getStyleClass().add("card-text");

        Runnable refreshCount = () ->
                countLabel.setText(rows.size() + (rows.size() == 1 ? " ticket type" : " ticket types"));
        refreshCount.run();

        Button addBtn = new Button("+ Add Ticket Type");
        addBtn.getStyleClass().add("primary-btn");
        addBtn.setOnAction(e -> {
            TicketTypeRow row = new TicketTypeRow("", "0");
            row.removeButton.setOnAction(removeEvent -> {
                rows.remove(row);
                rowsBox.getChildren().remove(row.container);
                refreshCount.run();
            });
            rows.add(row);
            rowsBox.getChildren().add(row.container);
            refreshCount.run();
        });

        for (TicketTypeRow row : rows) {
            row.removeButton.setOnAction(removeEvent -> {
                rows.remove(row);
                rowsBox.getChildren().remove(row.container);
                refreshCount.run();
            });
        }

        final LinkedHashMap<String, String>[] resultHolder = new LinkedHashMap[]{null};

        Button saveBtn = new Button("Save Ticket Types");
        saveBtn.getStyleClass().add("primary-btn");
        saveBtn.setOnAction(e -> {
            LinkedHashMap<String, String> mapped = collectTicketTypes(rows);
            if (mapped == null || !mapped.containsKey("Standard")) {
                AlertHelper.showError("Ticket Types", "A Standard ticket type is required.");
                return;
            }

            resultHolder[0] = mapped;
            form.ticketTypePrices = new LinkedHashMap<>(mapped);
            if (targetEvent != null) {
                ticketController.setConfiguredTicketTypesForEvent(targetEvent, mapped);
            }
        });

        Button doneBtn = new Button("Done - Back to Event Form");
        doneBtn.getStyleClass().add("secondary-btn");
        doneBtn.setOnAction(e -> {
            LinkedHashMap<String, String> mapped = collectTicketTypes(rows);
            if (mapped == null || !mapped.containsKey("Standard")) {
                AlertHelper.showError("Ticket Types", "A Standard ticket type is required.");
                return;
            }

            resultHolder[0] = mapped;
            form.ticketTypePrices = new LinkedHashMap<>(mapped);
            if (targetEvent != null) {
                ticketController.setConfiguredTicketTypesForEvent(targetEvent, mapped);
            }
            popup.close();
        });

        HBox footer = new HBox(12, saveBtn, doneBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);

        ScrollPane rowsScrollPane = new ScrollPane(rowsBox);
        rowsScrollPane.setFitToWidth(true);
        rowsScrollPane.setPrefViewportHeight(260);
        rowsScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        card.getChildren().addAll(
                header,
                subtitle,
                section,
                sectionSub,
                addBtn,
                rowsScrollPane,
                new Separator(),
                countLabel,
                footer
        );

        root.getChildren().add(card);

        Scene scene = new Scene(root, 980, 620);
        if (ownerScene != null) {
            scene.getStylesheets().addAll(ownerScene.getStylesheets());
        }

        popup.setScene(scene);
        popup.showAndWait();

        return resultHolder[0];
    }

    private LinkedHashMap<String, String> collectTicketTypes(List<TicketTypeRow> rows) {
        if (rows.isEmpty()) {
            AlertHelper.showError("Ticket Types", "Please add at least one ticket type.");
            return null;
        }

        LinkedHashMap<String, String> mapped = new LinkedHashMap<>();

        for (TicketTypeRow row : rows) {
            String type = row.nameField.getText().trim();
            String rawPrice = row.priceField.getText().trim();

            if (type.isBlank()) {
                AlertHelper.showError("Ticket Types", "Ticket type name cannot be empty.");
                return null;
            }

            if (mapped.containsKey(type)) {
                AlertHelper.showError("Ticket Types", "Ticket type names must be unique.");
                return null;
            }

            try {
                double value = Double.parseDouble(rawPrice.replace(",", "."));
                if (value < 0) {
                    AlertHelper.showError("Ticket Types", "Price cannot be negative.");
                    return null;
                }
                mapped.put(type, formatPrice(value));
            } catch (NumberFormatException ex) {
                AlertHelper.showError("Ticket Types", "Price must be a valid number.");
                return null;
            }
        }

        return mapped;
    }

    private void createEventFromForm(EventEditorForm form) {
        String validation = eventController.validateEvent(
                form.titleField.getText().trim(),
                form.startDatePicker.getValue() == null ? "" : form.startDatePicker.getValue().format(FORM_DATE),
                form.startTimeBox.getValue(),
                form.locationField.getText().trim(),
                form.capacityField.getText().trim(),
                form.priceField.getText().trim()
        );

        if (validation != null) {
            AlertHelper.showError("Invalid Event", validation);
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(
                form.startDatePicker.getValue(),
                LocalTime.parse(form.startTimeBox.getValue(), TIME_FORMATTER)
        );

        if (startDateTime.isBefore(LocalDateTime.now())) {
            AlertHelper.showError("Invalid Event", "The event start time cannot be in the past.");
            return;
        }

        String endValidation = validateOptionalEndDateTime(form, startDateTime);
        if (endValidation != null) {
            AlertHelper.showError("Invalid Event", endValidation);
            return;
        }

        Event event = new Event(
                form.titleField.getText().trim(),
                buildDateTimeValue(form.startDatePicker.getValue(), form.startTimeBox.getValue()),
                buildDateTimeValue(form.endDatePicker.getValue(), form.endTimeBox.getValue()),
                form.locationField.getText().trim(),
                form.locationGuidanceField.getText().trim(),
                form.notesArea.getText().trim(),
                normalizePrice(form.priceField.getText()),
                form.capacityField.getText().trim(),
                "Available",
                getDefaultCoordinatorAssignments()
        );

        form.ticketTypePrices.put("Standard", normalizePrice(form.priceField.getText()));
        LinkedHashMap<String, String> configuredTypes = collectTicketTypes(form.ticketTypeRowsForSave());
        if (configuredTypes == null) {
            return;
        }

        eventController.createEvent(event);
        syncStoredEventStatus(event);
        ticketController.setConfiguredTicketTypesForEvent(event, configuredTypes);
        mainView.showCoordinatorDashboard("Events");
    }

    private void updateEventFromForm(Event currentEvent, EventEditorForm form) {
        String validation = eventController.validateEvent(
                form.titleField.getText().trim(),
                form.startDatePicker.getValue() == null ? "" : form.startDatePicker.getValue().format(FORM_DATE),
                form.startTimeBox.getValue(),
                form.locationField.getText().trim(),
                form.capacityField.getText().trim(),
                form.priceField.getText().trim()
        );

        if (validation != null) {
            AlertHelper.showError("Invalid Event", validation);
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(
                form.startDatePicker.getValue(),
                LocalTime.parse(form.startTimeBox.getValue(), TIME_FORMATTER)
        );

        if (startDateTime.isBefore(LocalDateTime.now())) {
            AlertHelper.showError("Invalid Event", "The event start time cannot be in the past.");
            return;
        }

        String endValidation = validateOptionalEndDateTime(form, startDateTime);
        if (endValidation != null) {
            AlertHelper.showError("Invalid Event", endValidation);
            return;
        }

        Event updatedEvent = new Event(
                form.titleField.getText().trim(),
                buildDateTimeValue(form.startDatePicker.getValue(), form.startTimeBox.getValue()),
                buildDateTimeValue(form.endDatePicker.getValue(), form.endTimeBox.getValue()),
                form.locationField.getText().trim(),
                form.locationGuidanceField.getText().trim(),
                form.notesArea.getText().trim(),
                normalizePrice(form.priceField.getText()),
                form.capacityField.getText().trim(),
                currentEvent.getStatus(),
                currentEvent.getCoordinators()
        );

        form.ticketTypePrices.put("Standard", normalizePrice(form.priceField.getText()));
        LinkedHashMap<String, String> configuredTypes = collectTicketTypes(form.ticketTypeRowsForSave());
        if (configuredTypes == null) {
            return;
        }

        boolean updated = eventController.updateEvent(currentEvent, updatedEvent);
        if (!updated) {
            AlertHelper.showError("Update Failed", "The selected event could not be updated.");
            return;
        }

        ticketController.moveConfiguredTicketTypes(currentEvent, updatedEvent);
        ticketController.setConfiguredTicketTypesForEvent(updatedEvent, configuredTypes);
        syncStoredEventStatus(updatedEvent);

        mainView.clearEditingEvent();
        mainView.showCoordinatorDashboard("Events");
    }

    private String validateOptionalEndDateTime(EventEditorForm form, LocalDateTime startDateTime) {
        boolean hasEndDate = form.endDatePicker.getValue() != null;
        boolean hasEndTime = form.endTimeBox.getValue() != null && !form.endTimeBox.getValue().isBlank();

        if (!hasEndDate && !hasEndTime) {
            return null;
        }

        if (!hasEndDate || !hasEndTime) {
            return "Please choose both end date and end time, or leave both empty.";
        }

        LocalDateTime endDateTime = LocalDateTime.of(
                form.endDatePicker.getValue(),
                LocalTime.parse(form.endTimeBox.getValue(), TIME_FORMATTER)
        );

        if (endDateTime.isBefore(startDateTime)) {
            return "The end date and time must be after the event start.";
        }

        return null;
    }

    private String buildDateTimeValue(LocalDate date, String time) {
        if (date == null || time == null || time.isBlank()) {
            return "";
        }

        LocalTime selectedTime = LocalTime.parse(time, TIME_FORMATTER);
        return DISPLAY_DATE_TIME.format(LocalDateTime.of(date, selectedTime));
    }

    private void setDateTimeFields(String dateTimeValue, DatePicker datePicker, ComboBox<String> timeBox) {
        if (dateTimeValue == null || dateTimeValue.isBlank()) {
            return;
        }

        LocalDateTime parsedDateTime = parseDateTimeValue(dateTimeValue);
        if (parsedDateTime != null) {
            datePicker.setValue(parsedDateTime.toLocalDate());
            timeBox.setValue(parsedDateTime.toLocalTime().format(TIME_FORMATTER));
        }
    }

    private LocalDateTime parseDateTimeValue(String dateTimeValue) {
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{
                DISPLAY_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
        }) {
            try {
                return LocalDateTime.parse(dateTimeValue.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        try {
            return LocalDateTime.parse(dateTimeValue.trim().replace(" ", "T"));
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(dateTimeValue.trim(), FORM_DATE).atStartOfDay();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String normalizePrice(String rawPrice) {
        double value = Double.parseDouble(rawPrice.trim().replace(",", "."));
        return formatPrice(value);
    }

    private String formatPrice(double value) {
        if (value == 0) {
            return "Free";
        }
        if (value == Math.floor(value)) {
            return String.format(Locale.ENGLISH, "%.0f DKK", value);
        }
        return String.format(Locale.ENGLISH, "%.2f DKK", value);
    }

    private String toEditablePrice(String priceText) {
        if (priceText == null || priceText.isBlank() || "Free".equalsIgnoreCase(priceText.trim())) {
            return "0";
        }

        return priceText
                .replace("DKK", "")
                .replace("dkk", "")
                .trim();
    }

    private boolean matchesSearch(Event event, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String needle = query.trim().toLowerCase(Locale.ENGLISH);
        return safeText(event.getTitle()).toLowerCase(Locale.ENGLISH).contains(needle)
                || safeText(event.getLocation()).toLowerCase(Locale.ENGLISH).contains(needle);
    }

    private String formatCompactDateTime(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return "";
        }
        return dateTime.replace(" at ", ", ");
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String trimmed = email.trim();
        return trimmed.contains("@")
                && trimmed.contains(".")
                && trimmed.indexOf('@') > 0
                && trimmed.indexOf('.') > trimmed.indexOf('@') + 1;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String displayText(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private Label buildTicketTypeSummaryLabel(LinkedHashMap<String, String> ticketTypes) {
        Label label = new Label(buildTicketTypeSummaryText(ticketTypes));
        label.getStyleClass().add("card-text");
        label.setWrapText(true);
        return label;
    }

    private String buildTicketTypeSummaryText(LinkedHashMap<String, String> ticketTypes) {
        if (ticketTypes == null || ticketTypes.isEmpty()) {
            return "No ticket types configured yet.";
        }

        List<String> summaries = new ArrayList<>();
        for (Map.Entry<String, String> entry : ticketTypes.entrySet()) {
            summaries.add(entry.getKey() + " (" + entry.getValue() + ")");
        }
        return String.join(" | ", summaries);
    }

    private void syncStoredEventStatus(Event event) {
        String status = ticketController.getEventStatus(event);
        event.setStatus(status);
        eventController.updateEventStatus(event, status);
    }

    private String statusStyleClass(String status) {
        return switch (status) {
            case "Sold Out" -> "status-sold";
            case "Fast Selling", "Selling Fast" -> "status-fast";
            default -> "status-avail";
        };
    }

    private VBox fieldBox(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(8);
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinWidth(0);

        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setPrefWidth(Region.USE_COMPUTED_SIZE);
            region.setMinWidth(0);
        }

        box.getChildren().addAll(label, field);
        return box;
    }

    private HBox twoColRow(VBox left, VBox right) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);
        row.setFillHeight(true);

        left.setFillWidth(true);
        right.setFillWidth(true);

        left.setMaxWidth(Double.MAX_VALUE);
        right.setMaxWidth(Double.MAX_VALUE);

        left.setMinWidth(0);
        right.setMinWidth(0);

        left.prefWidthProperty().bind(row.widthProperty().subtract(20).divide(2));
        right.prefWidthProperty().bind(row.widthProperty().subtract(20).divide(2));

        row.getChildren().addAll(left, right);
        return row;
    }

    private List<String> generateTimes() {
        List<String> values = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                values.add(String.format(Locale.ENGLISH, "%02d:%02d", hour, minute));
            }
        }
        return values;
    }

    private void refreshStartTimeChoices(DatePicker startDatePicker, ComboBox<String> startTimeBox) {
        List<String> filtered = new ArrayList<>();
        LocalDate selectedDate = startDatePicker.getValue();
        LocalTime now = LocalTime.now();

        for (String value : generateTimes()) {
            LocalTime candidate = LocalTime.parse(value, TIME_FORMATTER);

            if (selectedDate != null && selectedDate.equals(LocalDate.now()) && !candidate.isAfter(now)) {
                continue;
            }

            filtered.add(value);
        }

        setFilteredTimes(startTimeBox, filtered);
    }

    private void refreshEndTimeChoices(DatePicker startDatePicker,
                                       ComboBox<String> startTimeBox,
                                       DatePicker endDatePicker,
                                       ComboBox<String> endTimeBox) {
        List<String> filtered = new ArrayList<>();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        LocalTime now = LocalTime.now();

        LocalTime selectedStartTime = null;
        if (startTimeBox.getValue() != null && !startTimeBox.getValue().isBlank()) {
            selectedStartTime = LocalTime.parse(startTimeBox.getValue(), TIME_FORMATTER);
        }

        for (String value : generateTimes()) {
            LocalTime candidate = LocalTime.parse(value, TIME_FORMATTER);

            if (endDate != null && endDate.equals(LocalDate.now()) && !candidate.isAfter(now)) {
                continue;
            }

            if (startDate != null
                    && endDate != null
                    && startDate.equals(endDate)
                    && selectedStartTime != null
                    && candidate.isBefore(selectedStartTime)) {
                continue;
            }

            filtered.add(value);
        }

        setFilteredTimes(endTimeBox, filtered);
    }

    private void setFilteredTimes(ComboBox<String> timeBox, List<String> filteredTimes) {
        String currentValue = timeBox.getValue();
        timeBox.setItems(FXCollections.observableArrayList(filteredTimes));

        if (currentValue != null && filteredTimes.contains(currentValue)) {
            timeBox.setValue(currentValue);
        } else {
            timeBox.setValue(null);
        }
    }

    private TextFormatter<String> numericFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String next = change.getControlNewText();
            return next.matches("\\d{0,6}([\\.,]\\d{0,2})?") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    private Region grow() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private GridPane createTwoColumnGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints first = new ColumnConstraints();
        first.setPercentWidth(50);
        first.setHgrow(Priority.ALWAYS);
        first.setFillWidth(true);

        ColumnConstraints second = new ColumnConstraints();
        second.setPercentWidth(50);
        second.setHgrow(Priority.ALWAYS);
        second.setFillWidth(true);

        grid.getColumnConstraints().setAll(first, second);
        return grid;
    }

    private void renderCoordinatorEventCards(GridPane grid, List<Event> events) {
        grid.getChildren().clear();

        if (events.isEmpty()) {
            Label empty = new Label("No events match your search.");
            empty.getStyleClass().add("card-text");
            grid.add(empty, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;

        for (Event event : events) {
            VBox card = createEventCard(event);
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setVgrow(card, Priority.ALWAYS);
            GridPane.setFillWidth(card, true);
            GridPane.setFillHeight(card, true);

            grid.add(card, column, row);

            column++;
            if (column == 2) {
                column = 0;
                row++;
            }
        }
    }

    private List<Event> getVisibleCoordinatorEvents() {
        User currentUser = mainView.getCurrentUser();

        List<Event> visible = new ArrayList<>(eventController.getEventsForUser(currentUser));
        if (currentUser == null) {
            return visible;
        }

        String currentName = safeText(currentUser.getName());
        String currentUsername = safeText(currentUser.getUsername());

        for (Event event : eventController.getEvents()) {
            if (event == null || event.getCoordinators() == null) {
                continue;
            }

            if (containsCoordinator(event.getCoordinators(), currentName, currentUsername)
                    && !containsSameEvent(visible, event)) {
                visible.add(event);
            }
        }

        return visible;
    }

    private boolean containsCoordinator(String[] coordinators, String currentName, String currentUsername) {
        for (String value : coordinators) {
            String candidate = safeText(value);
            if (candidate.equalsIgnoreCase(currentName) || candidate.equalsIgnoreCase(currentUsername)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSameEvent(List<Event> events, Event target) {
        for (Event event : events) {
            if (sameEvent(event, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameEvent(Event first, Event second) {
        if (first == null || second == null) {
            return false;
        }

        return safeText(first.getTitle()).equalsIgnoreCase(safeText(second.getTitle()))
                && safeText(first.getLocation()).equalsIgnoreCase(safeText(second.getLocation()))
                && safeText(first.getStartDateTime()).equalsIgnoreCase(safeText(second.getStartDateTime()));
    }

    private String[] getDefaultCoordinatorAssignments() {
        User currentUser = mainView.getCurrentUser();
        if (currentUser == null) {
            return new String[0];
        }

        if (!"Event Coordinator".equalsIgnoreCase(currentUser.getRole())) {
            return new String[0];
        }

        String currentName = currentUser.getName();
        if (currentName == null || currentName.isBlank()) {
            return new String[0];
        }

        return new String[]{currentName.trim()};
    }

    private final class EventEditorForm {
        private final TextField titleField = new TextField();
        private final DatePicker startDatePicker = new DatePicker();
        private final ComboBox<String> startTimeBox = new ComboBox<>(FXCollections.observableArrayList(generateTimes()));
        private final DatePicker endDatePicker = new DatePicker();
        private final ComboBox<String> endTimeBox = new ComboBox<>(FXCollections.observableArrayList(generateTimes()));
        private final TextField locationField = new TextField();
        private final TextField locationGuidanceField = new TextField();
        private final TextField capacityField = new TextField();
        private final TextField priceField = new TextField();
        private final TextArea notesArea = new TextArea();

        private final VBox titleBox;
        private final HBox startDateTimeRow;
        private final HBox endDateTimeRow;
        private final VBox locationBox;
        private final VBox locationGuidanceBox;
        private final HBox extraRow;
        private final VBox notesBox;

        private LinkedHashMap<String, String> ticketTypePrices = new LinkedHashMap<>();

        private EventEditorForm() {
            this(null);
        }

        private EventEditorForm(Event seedEvent) {
            titleField.getStyleClass().add("input-field");
            locationField.getStyleClass().add("input-field");
            locationGuidanceField.getStyleClass().add("input-field");
            capacityField.getStyleClass().add("input-field");
            startDatePicker.getStyleClass().add("dashboard-picker");
            startTimeBox.getStyleClass().add("dashboard-select");
            endDatePicker.getStyleClass().add("dashboard-picker");
            endTimeBox.getStyleClass().add("dashboard-select");
            priceField.getStyleClass().add("input-field");
            notesArea.getStyleClass().add("input-field");

            titleField.setMaxWidth(Double.MAX_VALUE);
            locationField.setMaxWidth(Double.MAX_VALUE);
            locationGuidanceField.setMaxWidth(Double.MAX_VALUE);
            capacityField.setMaxWidth(Double.MAX_VALUE);
            priceField.setMaxWidth(Double.MAX_VALUE);
            notesArea.setMaxWidth(Double.MAX_VALUE);
            notesArea.setPrefRowCount(5);
            notesArea.setWrapText(true);

            capacityField.setTextFormatter(numericFormatter());
            priceField.setTextFormatter(numericFormatter());

            startDatePicker.setMaxWidth(Double.MAX_VALUE);
            endDatePicker.setMaxWidth(Double.MAX_VALUE);
            startTimeBox.setMaxWidth(Double.MAX_VALUE);
            endTimeBox.setMaxWidth(Double.MAX_VALUE);

            startDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(empty || item.isBefore(LocalDate.now()));
                }
            });

            endDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);

                    LocalDate minimumDate = startDatePicker.getValue() == null
                            ? LocalDate.now()
                            : startDatePicker.getValue();

                    setDisable(empty || item.isBefore(minimumDate));
                }
            });

            startTimeBox.setPromptText("--:--");
            endTimeBox.setPromptText("--:--");

            if (seedEvent != null) {
                titleField.setText(seedEvent.getTitle());
                setDateTimeFields(seedEvent.getStartDateTime(), startDatePicker, startTimeBox);
                setDateTimeFields(seedEvent.getEndDateTime(), endDatePicker, endTimeBox);
                locationField.setText(seedEvent.getLocation());
                locationGuidanceField.setText(seedEvent.getLocationGuidance());
                notesArea.setText(seedEvent.getNotes());
                priceField.setText(toEditablePrice(seedEvent.getPrice()));
                capacityField.setText(seedEvent.getCapacity());

                LinkedHashMap<String, String> configured =
                        ticketController.getConfiguredTicketTypesForEvent(seedEvent);
                if (!configured.isEmpty()) {
                    ticketTypePrices.putAll(configured);
                }
            }

            startDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && endDatePicker.getValue() != null && endDatePicker.getValue().isBefore(newValue)) {
                    endDatePicker.setValue(null);
                }

                refreshStartTimeChoices(startDatePicker, startTimeBox);
                refreshEndTimeChoices(startDatePicker, startTimeBox, endDatePicker, endTimeBox);
            });

            startTimeBox.valueProperty().addListener((obs, oldValue, newValue) ->
                    refreshEndTimeChoices(startDatePicker, startTimeBox, endDatePicker, endTimeBox)
            );

            endDatePicker.valueProperty().addListener((obs, oldValue, newValue) ->
                    refreshEndTimeChoices(startDatePicker, startTimeBox, endDatePicker, endTimeBox)
            );

            refreshStartTimeChoices(startDatePicker, startTimeBox);
            refreshEndTimeChoices(startDatePicker, startTimeBox, endDatePicker, endTimeBox);

            if (ticketTypePrices.isEmpty()) {
                String defaultPrice = priceField.getText().isBlank() ? "0" : priceField.getText().trim();
                try {
                    ticketTypePrices.put("Standard", formatPrice(Double.parseDouble(defaultPrice.replace(",", "."))));
                } catch (NumberFormatException ex) {
                    ticketTypePrices.put("Standard", "Free");
                }
            }

            titleBox = fieldBox("Event Title *", titleField);
            startDateTimeRow = twoColRow(
                    fieldBox("Event Date *", startDatePicker),
                    fieldBox("Event Time *", startTimeBox)
            );
            endDateTimeRow = twoColRow(
                    fieldBox("End Date", endDatePicker),
                    fieldBox("End Time", endTimeBox)
            );
            locationBox = fieldBox("Venue *", locationField);
            locationGuidanceBox = fieldBox("Location Guidance", locationGuidanceField);
            extraRow = twoColRow(
                    fieldBox("Capacity *", capacityField),
                    fieldBox("Price (DKK) *", priceField)
            );
            notesBox = fieldBox("Notes", notesArea);
        }

        private List<TicketTypeRow> ticketTypeRowsForSave() {
            List<TicketTypeRow> rows = new ArrayList<>();
            for (Map.Entry<String, String> entry : ticketTypePrices.entrySet()) {
                rows.add(new TicketTypeRow(entry.getKey(), toEditablePrice(entry.getValue())));
            }
            return rows;
        }
    }

    private static final class TicketTypeRow {
        private final VBox container;
        private final TextField nameField;
        private final TextField priceField;
        private final Button removeButton;
        private final boolean standardRow;

        private TicketTypeRow(String name, String price) {
            standardRow = "Standard".equalsIgnoreCase(name == null ? "" : name.trim());

            nameField = new TextField(name);
            nameField.getStyleClass().add("input-field");
            nameField.setPromptText("Standard");
            nameField.setMaxWidth(Double.MAX_VALUE);
            if (standardRow) {
                nameField.getStyleClass().add("locked-field");
                nameField.setEditable(false);
                nameField.setFocusTraversable(false);
            }

            priceField = new TextField(price);
            priceField.getStyleClass().add("input-field");
            priceField.setPromptText("0");
            priceField.setMaxWidth(Double.MAX_VALUE);

            removeButton = new Button("Remove");
            removeButton.getStyleClass().add("danger-btn");
            removeButton.setPrefHeight(42);
            removeButton.setMinWidth(88);

            if (standardRow) {
                removeButton.setDisable(true);
                removeButton.setVisible(false);
                removeButton.setManaged(false);
            }

            Label nameLabel = new Label("Ticket Type");
            nameLabel.getStyleClass().add("form-label");

            VBox nameBox = new VBox(6, nameLabel, nameField);
            nameBox.setFillWidth(true);
            nameBox.setMaxWidth(Double.MAX_VALUE);
            nameBox.setMinWidth(0);
            HBox.setHgrow(nameBox, Priority.ALWAYS);

            Label priceLabel = new Label("Price (DKK)");
            priceLabel.getStyleClass().add("form-label");

            VBox priceBox = new VBox(6, priceLabel, priceField);
            priceBox.setFillWidth(true);
            priceBox.setMaxWidth(Double.MAX_VALUE);
            priceBox.setMinWidth(0);
            HBox.setHgrow(priceBox, Priority.ALWAYS);

            Label actionLabel = new Label(" ");
            actionLabel.getStyleClass().add("form-label");

            VBox actionBox = new VBox(6, actionLabel, removeButton);
            actionBox.setAlignment(Pos.TOP_LEFT);
            actionBox.setPrefWidth(110);
            actionBox.setMinWidth(110);
            actionBox.setMaxWidth(110);

            HBox row = new HBox(14, nameBox, priceBox, actionBox);
            row.setAlignment(Pos.TOP_LEFT);
            row.setFillHeight(false);

            container = new VBox(row);
            container.getStyleClass().addAll("event-card", "event-list-card");
            container.setPadding(new Insets(14));
            container.setMaxWidth(Double.MAX_VALUE);
        }
    }
}
