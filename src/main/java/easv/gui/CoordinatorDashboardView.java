
package easv.gui;

import easv.be.Event;
import easv.be.SoldTicketRecord;
import easv.be.Ticket;
import easv.be.User;
import easv.controller.EventController;
import easv.controller.TicketController;
import easv.controller.UserController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORM_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MainView mainView;
    private final EventController eventController;
    private final UserController userController;
    private final TicketController ticketController;
    private final String activeTab;

    public CoordinatorDashboardView(MainView mainView, EventController eventController,
                                    UserController userController, String activeTab) {
        this.mainView = mainView;
        this.eventController = eventController;
        this.userController = userController;
        this.ticketController = new TicketController();
        this.activeTab = activeTab;
    }

    public Parent getView() {
        javafx.scene.layout.BorderPane layout = new javafx.scene.layout.BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar());

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
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        layout.setCenter(scrollPane);

        return layout;
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

        sidebar.getChildren().addAll(logo, eventsBtn, createEventBtn, accessBtn, specialTicketsBtn, soldBtn, spacer, logoutBtn);
        return sidebar;
    }

    private VBox createEventsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events by title or venue...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setPrefWidth(520);

        HBox toolbar = new HBox(12, searchBar);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        for (Event event : eventController.getEvents()) {
            if (!matchesSearch(event, searchBar.getText())) {
                continue;
            }
            grid.getChildren().add(createEventCard(event));
        }

        searchBar.textProperty().addListener((obs, oldValue, newValue) -> {
            grid.getChildren().clear();
            for (Event event : eventController.getEvents()) {
                if (!matchesSearch(event, newValue)) {
                    continue;
                }
                grid.getChildren().add(createEventCard(event));
            }
        });

        content.getChildren().addAll(title, toolbar, grid);
        return content;
    }

    private VBox createManageAccessContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Manage Coordinator Access");
        title.getStyleClass().add("page-title");

        VBox list = new VBox(16);

        List<User> coordinators = userController.getUsersByRole("Event Coordinator");
        for (Event event : eventController.getEvents()) {
            list.getChildren().add(createAccessCard(event, coordinators));
        }

        content.getChildren().addAll(title, list);
        return content;
    }
    private VBox createSoldTicketsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        List<SoldTicketRecord> soldTickets = ticketController.getSoldTickets();
        int usedCount = 0;
        for (SoldTicketRecord ticket : soldTickets) {
            if (ticket.isUsed()) {
                usedCount++;
            }
        }
        int unusedCount = soldTickets.size() - usedCount;

        Label title = new Label("Sold Tickets");
        title.getStyleClass().add("page-title");

        Label count = new Label("\uD83C\uDFAB " + soldTickets.size() + (soldTickets.size() == 1 ? " ticket sold" : " tickets sold"));
        count.getStyleClass().add("card-text");

        HBox top = new HBox(title, grow(), count);
        top.setAlignment(Pos.CENTER_LEFT);

        Label statusSummary = new Label("Used: " + usedCount + "   |   Not Used: " + unusedCount);
        statusSummary.getStyleClass().add("card-text");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by public code, email, customer, event, or ticket type...");
        searchField.getStyleClass().add("search-bar");
        searchField.setPrefWidth(580);

        TextField onsiteEmailField = new TextField();
        onsiteEmailField.setPromptText("On-site validation by email");
        onsiteEmailField.getStyleClass().add("input-field");
        onsiteEmailField.setPrefWidth(260);

        Button emailLookupBtn = new Button("Find By Email");
        emailLookupBtn.getStyleClass().add("secondary-btn");
        emailLookupBtn.setOnAction(e -> {
            String email = onsiteEmailField.getText().trim();
            if (!isValidEmail(email)) {
                AlertHelper.showError("Invalid Email", "Please enter a valid email address for on-site validation.");
                return;
            }

            searchField.setText(email);
            boolean found = false;
            for (SoldTicketRecord ticket : soldTickets) {
                if (email.equalsIgnoreCase(ticket.getCustomerEmail())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                AlertHelper.showInfo("No Match", "No sold tickets were found for that email.");
            }
        });

        HBox searchRow = new HBox(10, searchField, onsiteEmailField, emailLookupBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        VBox bodyCard = new VBox(14);
        bodyCard.getStyleClass().addAll("event-card", "event-list-card");
        bodyCard.setPadding(new Insets(26));

        VBox list = new VBox(10);
        bodyCard.getChildren().add(list);

        Runnable render = () -> {
            list.getChildren().clear();
            String query = searchField.getText();

            if (soldTickets.isEmpty()) {
                VBox emptyBox = new VBox(10);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(50));

                Label icon = new Label("\uD83C\uDFAB");
                icon.getStyleClass().add("page-title");

                Label emptyTitle = new Label("No tickets sold yet");
                emptyTitle.getStyleClass().add("card-title");

                Label emptyText = new Label("Tickets sold through the Events section will appear here");
                emptyText.getStyleClass().add("card-text");

                emptyBox.getChildren().addAll(icon, emptyTitle, emptyText);
                list.getChildren().add(emptyBox);
                return;
            }

            int matches = 0;
            for (SoldTicketRecord ticket : soldTickets) {
                if (!matchesTicketSearch(ticket, query)) {
                    continue;
                }
                matches++;
                list.getChildren().add(createSoldTicketRow(ticket));
            }

            if (matches == 0) {
                Label none = new Label("No tickets match your search.");
                none.getStyleClass().add("card-text");
                list.getChildren().add(none);
            }
        };

        searchField.textProperty().addListener((obs, oldValue, newValue) -> render.run());
        render.run();

        content.getChildren().addAll(top, statusSummary, searchRow, bodyCard);
        return content;
    }

    private VBox createSpecialTicketsContent() {
        VBox content = new VBox();
        content.getChildren().add(new CreateSpecialTicketView(mainView).getView());
        return content;
    }

    private VBox createSoldTicketRow(SoldTicketRecord ticket) {
        VBox row = new VBox(8);
        row.getStyleClass().addAll("event-card", "event-list-card");
        row.setPadding(new Insets(14));

        String customerName = safeText(ticket.getCustomerName());
        String customerEmail = safeText(ticket.getCustomerEmail());
        Ticket originalTicket = ticketController.findByToken(ticket.getPublicCode());
        boolean isSpecialTicket = (originalTicket != null && originalTicket.isSpecialTicket())
                || (customerName.isBlank() && customerEmail.isBlank());
        String ticketLabel = isSpecialTicket ? "Special Ticket" : "Ticket Type";
        String detailsText = isSpecialTicket
                ? ticketLabel + ": " + safeText(ticket.getTicketType()) +
                    "  |  Price: " + safeText(ticket.getPrice())
                : ticketLabel + ": " + safeText(ticket.getTicketType()) +
                    "  |  Price: " + safeText(ticket.getPrice()) +
                    "  |  Customer: " + customerName + " (" + customerEmail + ")";

        Label title = new Label(ticket.getEventName() == null || ticket.getEventName().isBlank() ? "Unknown Event" : ticket.getEventName());
        title.getStyleClass().add("card-title");

        Label status = new Label(ticket.isUsed() ? "Used" : "Not Used");
        status.getStyleClass().add(ticket.isUsed() ? "status-fast" : "status-avail");

        HBox top = new HBox(10, title, grow(), status);
        top.setAlignment(Pos.CENTER_LEFT);

        Label details = new Label(detailsText);
        details.getStyleClass().add("card-text");

        Label id = new Label("Public Code: " + safeText(ticket.getPublicCode()));
        id.getStyleClass().add("notes-head");

        Button toggleUsageBtn = new Button(ticket.isUsed() ? "Mark As Not Used" : "Mark As Used");
        toggleUsageBtn.getStyleClass().add("secondary-btn");
        toggleUsageBtn.setOnAction(e -> {
            boolean nextUsed = !ticket.isUsed();
            boolean changed = ticketController.setSoldTicketUsedState(ticket.getPublicCode(), nextUsed);
            if (!changed) {
                AlertHelper.showError("Update Failed", "The ticket status could not be updated.");
                return;
            }
            mainView.showCoordinatorDashboard("Sold Tickets");
        });

        row.getChildren().addAll(top, details, id, toggleUsageBtn);
        return row;
    }

    private VBox createEventCard(Event event) {
        return createEventCard(event, true);
    }

    private VBox createEventCard(Event event, boolean allowDelete) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");

        HBox top = new HBox();
        Label titleLbl = new Label(event.getTitle());
        titleLbl.getStyleClass().add("card-title");

        String status = ticketController.getEventStatus(event);
        Label statusLbl = new Label(status);
        statusLbl.getStyleClass().add(statusStyleClass(status));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(titleLbl, spacer, statusLbl);

        VBox scheduleBox = new VBox(6);
        Label startLbl = new Label("\uD83D\uDD52 " + event.getStartDateTime());
        startLbl.getStyleClass().add("card-text");
        scheduleBox.getChildren().add(startLbl);

        VBox locationBox = new VBox(6);
        Label locationLbl = new Label("\uD83D\uDCCD " + event.getLocation());
        locationLbl.getStyleClass().add("card-text");
        locationBox.getChildren().add(locationLbl);

        Label notesHead = new Label("Notes");
        notesHead.getStyleClass().add("notes-head");

        Label notesLbl = new Label(event.getNotes());
        notesLbl.getStyleClass().add("card-text");
        notesLbl.setWrapText(true);

        Label priceLbl = new Label(event.getPrice());
        priceLbl.getStyleClass().add("price-text");

        // (Samu) Show the ticket types directly on the event card.
        Label ticketTypesHead = new Label("Ticket Types");
        ticketTypesHead.getStyleClass().add("notes-head");
        Label ticketTypesSummary = buildTicketTypeSummaryLabel(ticketController.getTicketTypePricesForEvent(event));

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

        card.getChildren().addAll(
                top,
                scheduleBox,
                locationBox,
                notesHead,
                notesLbl,
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
                mainView.showCoordinatorDashboard("Events");
            });

            card.getChildren().add(deleteBtn);
        }

        return card;
    }

    private VBox createAccessCard(Event event, List<User> coordinators) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("event-card", "event-list-card");

        Label title = new Label(event.getTitle());
        title.getStyleClass().add("card-title");

        Label date = new Label(formatCompactDateTime(event.getStartDateTime()));
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
    private Button createMenuBtn(String text, boolean isActive,
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
        content.setPadding(new Insets(30, 50, 30, 50));

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
            LinkedHashMap<String, String> updated = showTicketTypeConfigurationDialog(form.ticketTypePrices, configureTypesBtn.getScene(), form, null);
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
        content.setPadding(new Insets(30, 50, 30, 50));

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
            LinkedHashMap<String, String> updated = showTicketTypeConfigurationDialog(form.ticketTypePrices, configureTypesBtn.getScene(), form, seedEvent);
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

        Runnable refreshCount = () -> countLabel.setText(rows.size() + (rows.size() == 1 ? " ticket type" : " ticket types"));
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
            if (mapped == null) {
                return;
            }

            if (!mapped.containsKey("Standard")) {
                AlertHelper.showError("Ticket Types", "A Standard ticket type is required.");
                return;
            }

            resultHolder[0] = mapped;
            form.ticketTypePrices = new LinkedHashMap<>(mapped);
            if (targetEvent != null) {
                ticketController.setConfiguredTicketTypesForEvent(targetEvent, mapped);
            }
            AlertHelper.showInfo("Ticket Types Saved", "The ticket types were saved successfully.");
        });

        Button doneBtn = new Button("Done - Back to Event Form");
        doneBtn.getStyleClass().add("secondary-btn");
        doneBtn.setOnAction(e -> {
            LinkedHashMap<String, String> mapped = collectTicketTypes(rows);
            if (mapped == null) {
                return;
            }

            if (!mapped.containsKey("Standard")) {
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

        LocalDateTime startDateTime = LocalDateTime.of(form.startDatePicker.getValue(), LocalTime.parse(form.startTimeBox.getValue(), TIME_FORMATTER));
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
                new String[0]
        );

        form.ticketTypePrices.put("Standard", normalizePrice(form.priceField.getText()));
        LinkedHashMap<String, String> configuredTypes = collectTicketTypes(form.ticketTypeRowsForSave());
        if (configuredTypes == null) {
            return;
        }

        eventController.createEvent(event);
        syncStoredEventStatus(event);
        ticketController.setConfiguredTicketTypesForEvent(event, configuredTypes);
        AlertHelper.showInfo("Event Created", "The event was created successfully.");
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

        LocalDateTime startDateTime = LocalDateTime.of(form.startDatePicker.getValue(), LocalTime.parse(form.startTimeBox.getValue(), TIME_FORMATTER));
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
        AlertHelper.showInfo("Event Updated", "The event was updated successfully.");
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

        LocalDateTime endDateTime = LocalDateTime.of(form.endDatePicker.getValue(), LocalTime.parse(form.endTimeBox.getValue(), TIME_FORMATTER));
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
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{DISPLAY_DATE_TIME, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")}) {
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

    private boolean matchesTicketSearch(SoldTicketRecord ticket, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String needle = query.trim().toLowerCase(Locale.ENGLISH);
        String customerName = safeText(ticket.getCustomerName());
        String customerEmail = safeText(ticket.getCustomerEmail());
        String usedText = ticket.isUsed() ? "used" : "not used";

        return safeText(ticket.getPublicCode()).toLowerCase(Locale.ENGLISH).contains(needle)
                || safeText(ticket.getEventName()).toLowerCase(Locale.ENGLISH).contains(needle)
                || safeText(ticket.getTicketType()).toLowerCase(Locale.ENGLISH).contains(needle)
                || safeText(ticket.getPrice()).toLowerCase(Locale.ENGLISH).contains(needle)
                || customerName.toLowerCase(Locale.ENGLISH).contains(needle)
                || customerEmail.toLowerCase(Locale.ENGLISH).contains(needle)
                || usedText.contains(needle);
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
        return value == null ? "" : value;
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
            case "Fast Selling" -> "status-fast";
            default -> "status-avail";
        };
    }

    private VBox fieldBox(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(8);
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        box.getChildren().addAll(label, field);
        return box;
    }

    private HBox twoColRow(VBox left, VBox right) {
        HBox row = new HBox(20, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        left.setMaxWidth(Double.MAX_VALUE);
        right.setMaxWidth(Double.MAX_VALUE);
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
            titleField.setPromptText("Enter event title");
            locationField.setPromptText("Enter venue location");
            locationGuidanceField.setPromptText("Optional directions or meeting point");
            capacityField.setPromptText("e.g., 300");
            priceField.setPromptText("e.g., 150 (or 0 for free)");
            priceField.setTextFormatter(numericFormatter());

            notesArea.setPromptText("Add event description or notes");
            notesArea.setPrefRowCount(5);
            notesArea.getStyleClass().add("input-field");

            titleField.getStyleClass().add("input-field");
            locationField.getStyleClass().add("input-field");
            locationGuidanceField.getStyleClass().add("input-field");
            capacityField.getStyleClass().add("input-field");
            startDatePicker.getStyleClass().add("input-field");
            startTimeBox.getStyleClass().add("input-field");
            endDatePicker.getStyleClass().add("input-field");
            endTimeBox.getStyleClass().add("input-field");
            priceField.getStyleClass().add("input-field");

            startTimeBox.setPromptText("--:--");
            startTimeBox.setMaxWidth(Double.MAX_VALUE);
            endTimeBox.setPromptText("--:--");
            endTimeBox.setMaxWidth(Double.MAX_VALUE);

            if (seedEvent != null) {
                titleField.setText(seedEvent.getTitle());
                setDateTimeFields(seedEvent.getStartDateTime(), startDatePicker, startTimeBox);
                setDateTimeFields(seedEvent.getEndDateTime(), endDatePicker, endTimeBox);
                locationField.setText(seedEvent.getLocation());
                locationGuidanceField.setText(seedEvent.getLocationGuidance());
                notesArea.setText(seedEvent.getNotes());
                priceField.setText(toEditablePrice(seedEvent.getPrice()));
                capacityField.setText(seedEvent.getCapacity());

                LinkedHashMap<String, String> configured = ticketController.getConfiguredTicketTypesForEvent(seedEvent);
                if (!configured.isEmpty()) {
                    ticketTypePrices.putAll(configured);
                }
            }

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
            if (standardRow) {
                nameField.getStyleClass().add("locked-field");
                nameField.setEditable(false);
                nameField.setFocusTraversable(false);
            }

            priceField = new TextField(price);
            priceField.getStyleClass().add("input-field");
            priceField.setPromptText("0");

            removeButton = new Button("Remove");
            removeButton.getStyleClass().add("danger-btn");
            if (standardRow) {
                removeButton.setDisable(true);
                removeButton.setVisible(false);
                removeButton.setManaged(false);
            }

            Label nameLabel = new Label("Ticket Type");
            nameLabel.getStyleClass().add("form-label");
            VBox nameBox = new VBox(6, nameLabel, nameField);

            Label priceLabel = new Label("Price (DKK)");
            priceLabel.getStyleClass().add("form-label");
            VBox priceBox = new VBox(6, priceLabel, priceField);

            HBox row = new HBox(12, nameBox, priceBox, removeButton);
            row.setAlignment(Pos.BOTTOM_LEFT);
            HBox.setHgrow(nameBox, Priority.ALWAYS);
            HBox.setHgrow(priceBox, Priority.ALWAYS);

            container = new VBox(row);
            container.getStyleClass().addAll("event-card", "event-list-card");
            container.setPadding(new Insets(14));
        }
    }
}
