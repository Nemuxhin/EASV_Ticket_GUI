package easv.gui;

import easv.be.Event;
import easv.be.User;
import easv.controller.EventController;
import easv.controller.UserController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

public class CoordinatorDashboardView {
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORM_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MainView mainView;
    private final EventController eventController;
    private final UserController userController;
    private final String activeTab;

    public CoordinatorDashboardView(MainView mainView, EventController eventController,
                                    UserController userController, String activeTab) {
        this.mainView = mainView;
        this.eventController = eventController;
        this.userController = userController;
        this.activeTab = activeTab;
    }

    public Parent getView() {
        javafx.scene.layout.BorderPane layout = new javafx.scene.layout.BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar());

        VBox content = "Manage Access".equals(activeTab)
                ? createManageAccessContent()
                : "Edit Event".equals(activeTab)
                  ? createEditEventContent()
                  : "Create Event".equals(activeTab)
                    ? createCreateEventContent()
                    : createEventsContent();

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
                "Events".equals(activeTab) || "Create Event".equals(activeTab) || "Edit Event".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Events")
        );

        Button accessBtn = createMenuBtn(
                "\uD83D\uDC65 Manage Access",
                "Manage Access".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Manage Access")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("\uD83D\uDEAA Logout");
        logoutBtn.getStyleClass().add("sidebar-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> mainView.showPortalSelection());

        sidebar.getChildren().addAll(logo, eventsBtn, accessBtn, spacer, logoutBtn);
        return sidebar;
    }

    private VBox createEventsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(400);

        Button createBtn = new Button("\uFF0B Create Event");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setPrefWidth(1000);
        createBtn.setOnAction(e -> mainView.showCoordinatorDashboard("Create Event"));

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        for (Event event : eventController.getEvents()) {
            grid.getChildren().add(createEventCard(event));
        }

        content.getChildren().addAll(title, searchBar, createBtn, grid);
        return content;
    }

    private VBox createManageAccessContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Manage Access");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(400);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        for (Event event : eventController.getEvents()) {
            grid.getChildren().add(createAccessCard(event));
        }

        content.getChildren().addAll(title, searchBar, grid);
        return content;
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");

        HBox top = new HBox();
        Label titleLbl = new Label(event.getTitle());
        titleLbl.getStyleClass().add("card-title");

        Label statusLbl = new Label(event.getStatus());
        statusLbl.getStyleClass().add(
                "Available".equals(event.getStatus()) ? "status-avail" : "status-fast"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(titleLbl, spacer, statusLbl);

        VBox scheduleBox = new VBox(6);
        Label startLbl = new Label("\uD83D\uDD52 " + event.getStartDateTime());
        startLbl.getStyleClass().add("card-text");
        scheduleBox.getChildren().add(startLbl);
        if (event.hasEndDateTime()) {
            Label endLbl = new Label("Ends: " + event.getEndDateTime());
            endLbl.getStyleClass().add("card-text");
            scheduleBox.getChildren().add(endLbl);
        }

        VBox locationBox = new VBox(6);
        Label locationLbl = new Label("\uD83D\uDCCD " + event.getLocation());
        locationLbl.getStyleClass().add("card-text");
        locationBox.getChildren().add(locationLbl);
        if (event.hasLocationGuidance()) {
            Label guidanceLbl = new Label("Guidance: " + event.getLocationGuidance());
            guidanceLbl.getStyleClass().add("card-text");
            guidanceLbl.setWrapText(true);
            locationBox.getChildren().add(guidanceLbl);
        }

        Label notesHead = new Label("Notes");
        notesHead.getStyleClass().add("notes-head");

        Label notesLbl = new Label(event.getNotes());
        notesLbl.getStyleClass().add("card-text");
        notesLbl.setWrapText(true);

        Label priceLbl = new Label(event.getPrice());
        priceLbl.getStyleClass().add("price-text");

        Button sellBtn = new Button("Sell Ticket");
        sellBtn.getStyleClass().add("primary-btn");
        sellBtn.setMaxWidth(Double.MAX_VALUE);
        sellBtn.setOnAction(e -> mainView.showTicketSales(event));

        Button editBtn = new Button("Edit Event");
        editBtn.getStyleClass().add("secondary-btn");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.setOnAction(e -> mainView.showEditEvent(event));

        Button deleteBtn = new Button("\uD83D\uDDD1 Delete Event");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> {
            eventController.deleteEvent(event);
            mainView.showCoordinatorDashboard("Events");
        });

        card.getChildren().addAll(
                top,
                scheduleBox,
                locationBox,
                notesHead,
                notesLbl,
                new Separator(),
                priceLbl,
                sellBtn,
                editBtn,
                deleteBtn
        );

        return card;
    }

    private VBox createAccessCard(Event event) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");

        HBox top = new HBox();
        Label titleLbl = new Label(event.getTitle());
        titleLbl.getStyleClass().add("card-title");

        Label statusLbl = new Label(event.getStatus());
        statusLbl.getStyleClass().add(
                "Available".equals(event.getStatus()) ? "status-avail" : "status-fast"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(titleLbl, spacer, statusLbl);

        Label dateLbl = new Label("\uD83D\uDD52 " + event.getStartDateTime());
        dateLbl.getStyleClass().add("card-text");

        Label locationLbl = new Label("\uD83D\uDCCD " + event.getLocation());
        locationLbl.getStyleClass().add("card-text");

        Label assignedHead = new Label("Assigned Coordinators");
        assignedHead.getStyleClass().add("notes-head");

        FlowPane pillBox = new FlowPane(5, 5);
        if (event.getCoordinators() == null || event.getCoordinators().length == 0) {
            Label emptyLbl = new Label("No coordinators assigned yet");
            emptyLbl.getStyleClass().add("card-text");
            pillBox.getChildren().add(emptyLbl);
        } else {
            for (String coordinator : event.getCoordinators()) {
                Label pill = new Label(coordinator);
                pill.getStyleClass().add("coord-pill");
                pillBox.getChildren().add(pill);
            }
        }

        Button assignBtn = new Button("\uD83D\uDC65 Assign Coordinators");
        assignBtn.getStyleClass().add("primary-btn");
        assignBtn.setMaxWidth(Double.MAX_VALUE);
        assignBtn.setOnAction(e -> showAssignAccessDialog(event, assignBtn));

        Button deleteBtn = new Button("\uD83D\uDDD1 Delete Event");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> {
            eventController.deleteEvent(event);
            mainView.showCoordinatorDashboard("Manage Access");
        });

        card.getChildren().addAll(
                top,
                dateLbl,
                locationLbl,
                assignedHead,
                pillBox,
                assignBtn,
                deleteBtn
        );

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

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events by title or venue...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setPrefWidth(520);

        headerRow.getChildren().addAll(title, spacer, searchBar);

        VBox card = new VBox(18);
        card.getStyleClass().add("event-card");
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
                form.dateTimeRow,
                form.locationBox,
                form.extraRow,
                form.notesBox,
                actions
        );

        content.getChildren().addAll(headerRow, card);
        return content;
    }

    private VBox createEditEventContent() {
        Event seedEvent = mainView.getEditingEvent();
        if (seedEvent == null) {
            return createEventsContent();
        }

        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events by title or venue...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setPrefWidth(520);

        headerRow.getChildren().addAll(title, spacer, searchBar);

        VBox card = new VBox(18);
        card.getStyleClass().add("event-card");
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
                form.dateTimeRow,
                form.locationBox,
                form.extraRow,
                form.notesBox,
                actions
        );

        content.getChildren().addAll(headerRow, card);
        return content;
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

        Event event = new Event(
                form.titleField.getText().trim(),
                buildDateTimeValue(form.startDatePicker.getValue(), form.startTimeBox.getValue()),
                "",
                form.locationField.getText().trim(),
                "",
                form.notesArea.getText().trim(),
                normalizePrice(form.priceField.getText()),
                "Available",
                new String[0]
        );

        eventController.createEvent(event);
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

        Event updatedEvent = new Event(
                form.titleField.getText().trim(),
                buildDateTimeValue(form.startDatePicker.getValue(), form.startTimeBox.getValue()),
                "",
                form.locationField.getText().trim(),
                "",
                form.notesArea.getText().trim(),
                normalizePrice(form.priceField.getText()),
                currentEvent.getStatus(),
                currentEvent.getCoordinators()
        );

        boolean updated = eventController.updateEvent(currentEvent, updatedEvent);
        if (!updated) {
            AlertHelper.showError("Update Failed", "The selected event could not be updated.");
            return;
        }

        mainView.clearEditingEvent();
        mainView.showCoordinatorDashboard("Events");
    }

    private void showAssignAccessDialog(Event currentEvent, javafx.scene.Node ownerNode) {
        List<User> coordinators = userController.getUsersByRole("Event Coordinator");
        if (coordinators.isEmpty()) {
            showInfo("No event coordinators are available yet.");
            return;
        }

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.setResizable(false);

        if (ownerNode != null && ownerNode.getScene() != null && ownerNode.getScene().getWindow() != null) {
            popup.initOwner(ownerNode.getScene().getWindow());
        }

        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(30));
        overlay.setStyle("-fx-background-color: transparent;");

        VBox card = new VBox(18);
        card.getStyleClass().add("assign-dialog-card");
        card.setPrefWidth(460);
        card.setMaxWidth(460);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Assign Coordinators");
        title.getStyleClass().add("assign-dialog-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("assign-dialog-close");
        closeBtn.setOnAction(e -> popup.close());

        header.getChildren().addAll(title, spacer, closeBtn);

        Label subtitle = new Label("Click coordinators to assign or remove them.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("assign-dialog-subtitle");

        VBox pillPane = new VBox(12);

        List<String> currentAssignments = currentEvent.getCoordinators() == null
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(currentEvent.getCoordinators()));

        List<Button> selectorButtons = new ArrayList<>();
        List<User> buttonUsers = new ArrayList<>();

        for (User coordinator : coordinators) {
            Button pillBtn = new Button(coordinator.getName());
            pillBtn.getStyleClass().add("assign-pill");
            pillBtn.setMaxWidth(Double.MAX_VALUE);
            pillBtn.setPrefWidth(220);

            boolean isSelected = currentAssignments.contains(coordinator.getName());
            pillBtn.setUserData(isSelected);

            if (isSelected) {
                pillBtn.getStyleClass().add("assign-pill-selected");
            }

            pillBtn.setOnAction(e -> {
                boolean selected = (boolean) pillBtn.getUserData();
                if (selected) {
                    pillBtn.setUserData(false);
                    pillBtn.getStyleClass().remove("assign-pill-selected");
                } else {
                    pillBtn.setUserData(true);
                    if (!pillBtn.getStyleClass().contains("assign-pill-selected")) {
                        pillBtn.getStyleClass().add("assign-pill-selected");
                    }
                }
            });

            selectorButtons.add(pillBtn);
            buttonUsers.add(coordinator);
            pillPane.getChildren().add(pillBtn);
        }

        pillPane.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(pillPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(220);
        scrollPane.getStyleClass().add("assign-dialog-scroll");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");
        cancelBtn.setOnAction(e -> popup.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("primary-btn");
        saveBtn.setOnAction(e -> {
            List<String> selected = new ArrayList<>();

            for (int i = 0; i < selectorButtons.size(); i++) {
                if ((boolean) selectorButtons.get(i).getUserData()) {
                    selected.add(buttonUsers.get(i).getName());
                }
            }

            eventController.setCoordinators(currentEvent, selected.toArray(new String[0]));
            popup.close();
            mainView.showCoordinatorDashboard("Manage Access");
        });

        HBox actions = new HBox(12, cancelBtn, saveBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(header, subtitle, new Separator(), scrollPane, actions);
        overlay.getChildren().add(card);

        Scene scene = new Scene(overlay);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        if (ownerNode != null && ownerNode.getScene() != null) {
            scene.getStylesheets().addAll(ownerNode.getScene().getStylesheets());
        }

        popup.setScene(scene);
        popup.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Manage Access");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String validateEventInput(String title,
                                      LocalDate startDate,
                                      String startTime,
                                      LocalDate endDate,
                                      String endTime,
                                      String location,
                                      String notes,
                                      String price) {
        StringBuilder message = new StringBuilder();

        if (title == null || title.isBlank()) {
            message.append("- Title is required.\n");
        }
        if (startDate == null || startTime == null || startTime.isBlank()) {
            message.append("- Start date and time are required.\n");
        }
        boolean hasEndDate = endDate != null;
        boolean hasEndTime = endTime != null && !endTime.isBlank();
        if (hasEndDate != hasEndTime) {
            message.append("- End date and time must both be selected or both be empty.\n");
        }
        if (location == null || location.isBlank()) {
            message.append("- Location is required.\n");
        }
        if (price == null || price.isBlank()) {
            message.append("- Ticket price is required.\n");
        } else {
            try {
                double parsedPrice = Double.parseDouble(price.replace(",", "."));
                if (parsedPrice < 0) {
                    message.append("- Price cannot be negative.\n");
                }
            } catch (NumberFormatException ex) {
                message.append("- Price must be numeric.\n");
            }
        }

        if (message.length() > 0) {
            return message.toString().trim();
        }

        try {
            LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.parse(startTime, TIME_FORMATTER));
            if (startDateTime.isBefore(LocalDateTime.now())) {
                return "The event start time cannot be in the past.";
            }

            if (hasEndDate && hasEndTime) {
                LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.parse(endTime, TIME_FORMATTER));
                if (!endDateTime.isAfter(startDateTime)) {
                    return "The event end time must be after the start time.";
                }
            }
        } catch (DateTimeParseException ex) {
            return "Please use valid time values in HH:mm format.";
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

        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(dateTimeValue, DISPLAY_DATE_TIME);
            datePicker.setValue(parsedDateTime.toLocalDate());
            timeBox.setValue(parsedDateTime.toLocalTime().format(TIME_FORMATTER));
        } catch (DateTimeParseException ignored) {
        }
    }

    private String normalizePrice(String rawPrice) {
        double value = Double.parseDouble(rawPrice.trim().replace(",", "."));
        if (value == 0) {
            return "Free";
        }
        if (value == Math.floor(value)) {
            return String.format(Locale.ENGLISH, "%.0f DKK", value);
        }
        return String.format(Locale.ENGLISH, "%.2f DKK", value);
    }

    private final class EventEditorForm {
        private final TextField titleField = new TextField();
        private final DatePicker startDatePicker = new DatePicker();
        private final ComboBox<String> startTimeBox = new ComboBox<>(FXCollections.observableArrayList(generateTimes()));
        private final TextField locationField = new TextField();
        private final TextField capacityField = new TextField();
        private final TextField priceField = new TextField();
        private final TextArea notesArea = new TextArea();

        private final VBox titleBox;
        private final HBox dateTimeRow;
        private final VBox locationBox;
        private final HBox extraRow;
        private final VBox notesBox;

        private EventEditorForm() {
            this(null);
        }

        private EventEditorForm(Event seedEvent) {
            titleField.setPromptText("Enter event title");
            locationField.setPromptText("Enter venue location");
            capacityField.setPromptText("e.g., 300");
            priceField.setPromptText("e.g., 150 (or 0 for free)");
            priceField.setTextFormatter(numericFormatter());

            notesArea.setPromptText("Add event description or notes");
            notesArea.setPrefRowCount(5);
            notesArea.getStyleClass().add("input-field");

            titleField.getStyleClass().add("input-field");
            locationField.getStyleClass().add("input-field");
            capacityField.getStyleClass().add("input-field");
            startDatePicker.getStyleClass().add("input-field");
            startTimeBox.getStyleClass().add("input-field");
            priceField.getStyleClass().add("input-field");
            startTimeBox.setPromptText("--:--");
            startTimeBox.setMaxWidth(Double.MAX_VALUE);

            if (seedEvent != null) {
                titleField.setText(seedEvent.getTitle());
                setDateTimeFields(seedEvent.getStartDateTime(), startDatePicker, startTimeBox);
                locationField.setText(seedEvent.getLocation());
                notesArea.setText(seedEvent.getNotes());
                priceField.setText(seedEvent.getPrice().replace("DKK", "").replace("Free", "0").trim());
                capacityField.setText("300");
            }

            titleBox = fieldBox("Event Title *", titleField);
            dateTimeRow = twoColRow(
                    fieldBox("Event Date *", startDatePicker),
                    fieldBox("Event Time *", startTimeBox)
            );
            locationBox = fieldBox("Venue *", locationField);
            extraRow = twoColRow(
                    fieldBox("Capacity *", capacityField),
                    fieldBox("Price (DKK) *", priceField)
            );
            notesBox = fieldBox("Notes", notesArea);
        }
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
}