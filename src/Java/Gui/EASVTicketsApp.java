package Java.Gui;

import Java.Be.Event;
import Java.Controller.EventController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.*;
import java.time.format.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class EASVTicketsApp extends Application {
    private static final String[][] AVAILABLE_COORDINATORS = {
            {"Event Coordinator 1", "coordinator1@easv.dk"},
            {"Event Coordinator 2", "coordinator2@easv.dk"},
            {"Event Coordinator 3", "coordinator3@easv.dk"},
            {"Event Coordinator 4", "coordinator4@easv.dk"},
            {"Event Coordinator 5", "coordinator5@easv.dk"}
    };

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final EventController eventController = new EventController();
    private final StackPane rootPane = new StackPane();
    private Stage window;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        showPortalSelection();

        Scene mainScene = new Scene(rootPane, 1200, 800);
        mainScene.getStylesheets().add(getClass().getResource("/CSS/easv-style.css").toExternalForm());

        window.setTitle("EASV Ticket Management System");
        window.setScene(mainScene);
        window.show();
    }

    // Portal selection: user chooses the dashboard to open.
    private void showPortalSelection() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-bg");

        VBox header = ViewFactory.createHeader(
                "EASV Tickets",
                "Event Ticket Management System",
                "Select Portal",
                "Choose your access level"
        );

        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER);

        VBox admin = ViewFactory.createPortalCard("👤", "Admin", "Manage events");
        admin.setOnMouseClicked(e -> showAdminDashboard("Events"));

        VBox coordinator = ViewFactory.createPortalCard("📅", "Event Coordinator", "Create and manage events");
        coordinator.setOnMouseClicked(e -> showCoordinatorDashboard("Events"));

        VBox customer = ViewFactory.createPortalCard("🛍️", "Customer", "Buy tickets");
        customer.setOnMouseClicked(e -> showCustomerDashboard());

        cards.getChildren().addAll(admin, coordinator, customer);
        layout.getChildren().addAll(header, cards);
        rootPane.getChildren().setAll(layout);
    }

    // Admin dashboard uses the sidebar to switch between simple sections.
    private void showAdminDashboard(String activeTab) {
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar("Admin", activeTab));

        VBox content;
        if ("Events".equals(activeTab)) {
            content = createDashboardContent("Manage Events", "ADMIN_EVENTS");
        } else {
            content = createCoordinatorsContent();
        }

        layout.setCenter(createScrollPane(content));
        rootPane.getChildren().setAll(layout);
    }

    // Coordinator dashboard contains event management and access management.
    private void showCoordinatorDashboard(String activeTab) {
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar("Event Coordinator", activeTab));

        VBox content;
        if ("Events".equals(activeTab)) {
            content = createDashboardContent("Events", "COORD_EVENTS");
        } else {
            content = createDashboardContent("Manage Access", "COORD_ACCESS");
        }

        layout.setCenter(createScrollPane(content));
        rootPane.getChildren().setAll(layout);
    }

    // Customer overview is read-only and uses the same shared event list.
    private void showCustomerDashboard() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Button backButton = new Button("Back to Portal Selection");
        backButton.getStyleClass().add("secondary-btn");
        backButton.setOnAction(e -> showPortalSelection());

        Label title = new Label("Available Events");
        title.getStyleClass().add("page-title");

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        for (Event event : eventController.getEvents()) {
            grid.getChildren().add(buildEventCard(event, "CUSTOMER"));
        }

        content.getChildren().addAll(backButton, title, grid);
        rootPane.getChildren().setAll(createScrollPane(content));
    }

    // Buy ticket stays simple because the current work is focused on event management.
    private void showBuyTicket(Event event) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getStyleClass().add("main-bg");

        Label title = new Label("Buy Ticket");
        title.getStyleClass().add("page-title");

        Label eventTitle = new Label(event.getTitle());
        eventTitle.getStyleClass().add("card-title");

        Label price = ViewFactory.createPriceLabel(event.getPrice());

        final int[] quantity = {1};
        Label quantityLabel = new Label("1");
        Button minusButton = new Button("-");
        Button plusButton = new Button("+");
        minusButton.getStyleClass().add("qty-btn");
        plusButton.getStyleClass().add("qty-btn");

        minusButton.setOnAction(e -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                quantityLabel.setText(String.valueOf(quantity[0]));
            }
        });

        plusButton.setOnAction(e -> {
            quantity[0]++;
            quantityLabel.setText(String.valueOf(quantity[0]));
        });

        HBox quantityBox = new HBox(15, minusButton, quantityLabel, plusButton);
        quantityBox.setAlignment(Pos.CENTER);

        Button confirmButton = new Button("Confirm Purchase");
        confirmButton.getStyleClass().add("primary-btn");
        confirmButton.setOnAction(e -> showCustomerDashboard());

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("secondary-btn");
        backButton.setOnAction(e -> showCustomerDashboard());

        layout.getChildren().addAll(title, eventTitle, price, quantityBox, confirmButton, backButton);
        rootPane.getChildren().setAll(layout);
    }

    // Sidebar only handles navigation between the main sections.
    private VBox createSidebar(String role, String activeItem) {
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20));

        Label logo = new Label("EASV Tickets");
        logo.getStyleClass().add("sidebar-logo");

        Label roleLabel = new Label(role);
        roleLabel.getStyleClass().add("sidebar-sub");

        VBox menuBox = new VBox(10);
        if ("Admin".equals(role)) {
            menuBox.getChildren().add(ViewFactory.createMenuBtn("Coordinators", "Coordinators".equals(activeItem), e -> showAdminDashboard("Coordinators")));
            menuBox.getChildren().add(ViewFactory.createMenuBtn("Events", "Events".equals(activeItem), e -> showAdminDashboard("Events")));
        } else {
            menuBox.getChildren().add(ViewFactory.createMenuBtn("Events", "Events".equals(activeItem), e -> showCoordinatorDashboard("Events")));
            menuBox.getChildren().add(ViewFactory.createMenuBtn("Manage Access", "Manage Access".equals(activeItem), e -> showCoordinatorDashboard("Manage Access")));
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("Back to Portal Selection");
        backButton.getStyleClass().add("sidebar-logout");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(e -> showPortalSelection());

        sidebar.getChildren().addAll(logo, roleLabel, menuBox, spacer, backButton);
        return sidebar;
    }

    // This block stays separate because it does not use the event list.
    private VBox createCoordinatorsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Manage Coordinators");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search coordinators...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(400);

        Button createButton = new Button("Create Coordinator");
        createButton.getStyleClass().add("primary-btn");
        createButton.setPrefWidth(1000);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        for (String[] coordinator : AVAILABLE_COORDINATORS) {
            VBox card = new VBox(10);
            card.getStyleClass().add("event-card");

            Label nameLabel = new Label(coordinator[0]);
            nameLabel.getStyleClass().add("card-title");
            Label emailLabel = new Label(coordinator[1]);
            emailLabel.getStyleClass().add("card-text");

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("danger-btn");
            deleteButton.setMaxWidth(Double.MAX_VALUE);

            card.getChildren().addAll(nameLabel, emailLabel, new Separator(), deleteButton);
            grid.getChildren().add(card);
        }

        content.getChildren().addAll(title, searchBar, createButton, grid);
        return content;
    }

    // Shared event overview used by admin and coordinator screens.
    private VBox createDashboardContent(String titleText, String viewMode) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label(titleText);
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(400);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        content.getChildren().addAll(title, searchBar);

        if ("COORD_EVENTS".equals(viewMode)) {
            content.getChildren().add(createCreateEventButton(viewMode));
        }

        for (Event event : eventController.getEvents()) {
            grid.getChildren().add(buildEventCard(event, viewMode));
        }

        content.getChildren().add(grid);
        return content;
    }

    // Each screen adds its own actions on top of the same base card.
    private VBox buildEventCard(Event event, String viewMode) {
        VBox card = ViewFactory.createEventCardBase(event);
        VBox actionBox = new VBox(10);
        actionBox.setFillWidth(true);

        if ("CUSTOMER".equals(viewMode)) {
            Button buyButton = new Button("Buy Ticket");
            buyButton.getStyleClass().add("primary-btn");
            buyButton.setMaxWidth(Double.MAX_VALUE);
            buyButton.setOnAction(e -> showBuyTicket(event));

            actionBox.getChildren().addAll(ViewFactory.createPriceLabel(event.getPrice()), buyButton);
        } else if ("ADMIN_EVENTS".equals(viewMode)) {
            actionBox.getChildren().addAll(
                    ViewFactory.createPriceLabel(event.getPrice()),
                    createDeleteEventButton(event, viewMode)
            );
        } else if ("COORD_EVENTS".equals(viewMode)) {
            actionBox.getChildren().addAll(
                    ViewFactory.createPriceLabel(event.getPrice()),
                    createEditEventButton(event, viewMode),
                    createDeleteEventButton(event, viewMode)
            );
        } else if ("COORD_ACCESS".equals(viewMode)) {
            Label coordinatorTitle = new Label("Assigned Coordinators");
            coordinatorTitle.getStyleClass().add("notes-head");

            card.getChildren().addAll(
                    coordinatorTitle,
                    ViewFactory.createCoordinatorPillBox(event.getCoordinators())
            );

            actionBox.getChildren().addAll(
                    createAssignAccessButton(event, viewMode),
                    createDeleteEventButton(event, viewMode)
            );
        }

        addCardFooter(card, actionBox, viewMode);
        return card;
    }

    // SAMU: A shared footer keeps price and buttons aligned in every card.
    private void addCardFooter(VBox card, VBox actionBox, String viewMode) {
        if (actionBox.getChildren().isEmpty()) {
            return;
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        card.setFillWidth(true);
        card.setMinHeight(getCardMinHeight(viewMode));
        card.getChildren().addAll(spacer, new Separator(), actionBox);
    }

    private double getCardMinHeight(String viewMode) {
        if ("COORD_ACCESS".equals(viewMode)) {
            return 480;
        }
        if ("COORD_EVENTS".equals(viewMode)) {
            return 470;
        }
        return 430;
    }

    private ScrollPane createScrollPane(Region content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        return scrollPane;
    }

    // ==========================================
    // SAMU TASKS: CREATE / SHOW / DELETE EVENTS
    // ==========================================
    // SAMU: Coordinator can open the create-event form from the event overview.
    private Button createCreateEventButton(String viewMode) {
        Button createButton = new Button("Create Event");
        createButton.getStyleClass().add("primary-btn");
        createButton.setPrefWidth(1000);
        createButton.setOnAction(e -> showCreateEventDialog(viewMode));
        return createButton;
    }

    // SAMU: Coordinator can edit an event if something was typed wrong.
    private Button createEditEventButton(Event event, String viewMode) {
        Button editButton = new Button("Edit Event");
        editButton.getStyleClass().add("secondary-btn");
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setOnAction(e -> showEditEventDialog(event, viewMode));
        return editButton;
    }

    // SAMU: Access is assigned from available coordinator options.
    private Button createAssignAccessButton(Event event, String viewMode) {
        Button assignButton = new Button("Assign Access");
        assignButton.getStyleClass().add("primary-btn");
        assignButton.setMaxWidth(Double.MAX_VALUE);
        assignButton.setOnAction(e -> showAssignAccessDialog(event, viewMode));
        return assignButton;
    }

    // SAMU: Create uses the shared dialog so the form stays simple in one place.
    private void showCreateEventDialog(String viewMode) {
        Optional<Event> result = showEventDialog("Create Event", null);
        if (result.isPresent()) {
            eventController.addEvent(result.get());
            refreshView(viewMode);
        }
    }

    // SAMU: Edit reuses the same form and only replaces the selected event.
    private void showEditEventDialog(Event currentEvent, String viewMode) {
        Optional<Event> result = showEventDialog("Edit Event", currentEvent);
        if (result.isPresent()) {
            boolean updated = eventController.updateEvent(currentEvent, result.get());
            if (updated) {
                refreshView(viewMode);
            } else {
                showErrorMessage("Update failed", "The selected event could not be updated.");
            }
        }
    }

    // SAMU: Date and time now use simple choices instead of free text.
    private Optional<Event> showEventDialog(String dialogTitle, Event currentEvent) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.initOwner(window);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(dialogTitle);
        dialog.setHeaderText("Fill in the event details");

        ButtonType saveButtonType = new ButtonType("Save Event", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));

        TextField titleField = new TextField();
        DatePicker startDatePicker = new DatePicker();
        ComboBox<String> startTimeBox = createTimeBox();
        DatePicker endDatePicker = new DatePicker();
        ComboBox<String> endTimeBox = createTimeBox();
        TextField locationField = new TextField();
        TextField locationGuidanceField = new TextField();
        TextArea notesField = new TextArea();
        notesField.setWrapText(true);
        notesField.setPrefRowCount(3);
        TextField priceField = new TextField();
        priceField.setPromptText("Example: 150 or 150.50");
        priceField.setTextFormatter(createPriceTextFormatter());
        CheckBox[] coordinatorBoxes = createCoordinatorBoxes(currentEvent == null ? new String[0] : currentEvent.getCoordinators());

        FlowPane coordinatorSelection = new FlowPane(10, 10);
        for (CheckBox coordinatorBox : coordinatorBoxes) {
            coordinatorSelection.getChildren().add(coordinatorBox);
        }

        HBox startDateTimeBox = new HBox(10, startDatePicker, startTimeBox);
        HBox endDateTimeBox = new HBox(10, endDatePicker, endTimeBox);

        form.addRow(0, new Label("Title *"), titleField);
        form.addRow(1, new Label("Start date/time *"), startDateTimeBox);
        form.addRow(2, new Label("End date/time"), endDateTimeBox);
        form.addRow(3, new Label("Location *"), locationField);
        form.addRow(4, new Label("Location guidance"), locationGuidanceField);
        form.addRow(5, new Label("Notes *"), notesField);
        form.addRow(6, new Label("Price *"), priceField);
        form.addRow(7, new Label("Coordinators"), coordinatorSelection);

        if (currentEvent != null) {
            titleField.setText(currentEvent.getTitle());
            setDateTimeFields(currentEvent.getStartDateTime(), startDatePicker, startTimeBox);
            setDateTimeFields(currentEvent.getEndDateTime(), endDatePicker, endTimeBox);
            locationField.setText(currentEvent.getLocation());
            locationGuidanceField.setText(currentEvent.getLocationGuidance());
            notesField.setText(currentEvent.getNotes());
            priceField.setText(currentEvent.getPrice());
        }

        dialog.getDialogPane().setContent(form);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String validationMessage = validateRequiredInput(
                    titleField.getText(),
                    startDatePicker.getValue(),
                    startTimeBox.getValue(),
                    endDatePicker.getValue(),
                    endTimeBox.getValue(),
                    locationField.getText(),
                    notesField.getText(),
                    priceField.getText()
            );

            if (!validationMessage.isEmpty()) {
                e.consume();
                showErrorMessage("Invalid input", validationMessage);
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new Event(
                        titleField.getText().trim(),
                        buildDateTimeValue(startDatePicker.getValue(), startTimeBox.getValue()),
                        buildDateTimeValue(endDatePicker.getValue(), endTimeBox.getValue()),
                        locationField.getText().trim(),
                        locationGuidanceField.getText().trim(),
                        notesField.getText().trim(),
                        normalizePriceValue(priceField.getText()),
                        collectSelectedCoordinators(coordinatorBoxes)
                );
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // SAMU: Access can be updated from real coordinator options.
    private void showAssignAccessDialog(Event currentEvent, String viewMode) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.initOwner(window);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Assign Access");
        dialog.setHeaderText("Select event coordinators");

        ButtonType saveButtonType = new ButtonType("Save Access", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        CheckBox[] coordinatorBoxes = createCoordinatorBoxes(currentEvent.getCoordinators());
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        for (CheckBox coordinatorBox : coordinatorBoxes) {
            content.getChildren().add(coordinatorBox);
        }

        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(buttonType -> buttonType == saveButtonType ? collectSelectedCoordinators(coordinatorBoxes) : null);

        Optional<String[]> result = dialog.showAndWait();
        if (result.isPresent()) {
            Event updatedEvent = new Event(
                    currentEvent.getTitle(),
                    currentEvent.getStartDateTime(),
                    currentEvent.getEndDateTime(),
                    currentEvent.getLocation(),
                    currentEvent.getLocationGuidance(),
                    currentEvent.getNotes(),
                    currentEvent.getPrice(),
                    result.get()
            );

            boolean updated = eventController.updateEvent(currentEvent, updatedEvent);
            if (updated) {
                refreshView(viewMode);
            } else {
                showErrorMessage("Update failed", "The selected access could not be updated.");
            }
        }
    }

    // SAMU: Required fields stay simple, and price must be numeric.
    private String validateRequiredInput(String title, LocalDate startDate, String startTime,
                                         LocalDate endDate, String endTime, String location,
                                         String notes, String price) {
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
        if (notes == null || notes.isBlank()) {
            message.append("- Notes are required.\n");
        }
        if (price == null || price.isBlank()) {
            message.append("- Price is required.\n");
        } else if (!isValidPrice(price)) {
            message.append("- Price must be a valid number.\n");
        }

        return message.toString().trim();
    }

    // SAMU: This filter stops letters and keeps the price easy to validate.
    private TextFormatter<String> createPriceTextFormatter() {
        UnaryOperator<TextFormatter.Change> priceFilter = change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty() || newText.matches("\\d*(?:[.,]\\d{0,2})?")) {
                return change;
            }
            return null;
        };

        return new TextFormatter<>(priceFilter);
    }

    private boolean isValidPrice(String price) {
        return price != null && price.matches("\\d+(?:[.,]\\d{1,2})?");
    }

    private String normalizePriceValue(String price) {
        return price.trim().replace(',', '.');
    }

    private ComboBox<String> createTimeBox() {
        ComboBox<String> timeBox = new ComboBox<>();
        timeBox.setPrefWidth(120);

        for (int hour = 0; hour < 24; hour++) {
            timeBox.getItems().add(String.format("%02d:00", hour));
            timeBox.getItems().add(String.format("%02d:30", hour));
        }

        return timeBox;
    }

    private CheckBox[] createCoordinatorBoxes(String[] selectedCoordinators) {
        CheckBox[] coordinatorBoxes = new CheckBox[AVAILABLE_COORDINATORS.length];

        for (int i = 0; i < AVAILABLE_COORDINATORS.length; i++) {
            String coordinatorName = AVAILABLE_COORDINATORS[i][0];
            CheckBox coordinatorBox = new CheckBox(coordinatorName);
            coordinatorBox.setSelected(containsCoordinator(selectedCoordinators, coordinatorName));
            coordinatorBoxes[i] = coordinatorBox;
        }

        return coordinatorBoxes;
    }

    private boolean containsCoordinator(String[] coordinators, String coordinatorName) {
        for (String coordinator : coordinators) {
            if (coordinator.equals(coordinatorName)) {
                return true;
            }
        }
        return false;
    }

    private String[] collectSelectedCoordinators(CheckBox[] coordinatorBoxes) {
        List<String> selectedCoordinators = new ArrayList<>();

        for (CheckBox coordinatorBox : coordinatorBoxes) {
            if (coordinatorBox.isSelected()) {
                selectedCoordinators.add(coordinatorBox.getText());
            }
        }

        return selectedCoordinators.toArray(new String[0]);
    }

    private String buildDateTimeValue(LocalDate date, String time) {
        if (date == null || time == null || time.isBlank()) {
            return "";
        }

        LocalTime selectedTime = LocalTime.parse(time, TIME_FORMATTER);
        LocalDateTime dateTime = LocalDateTime.of(date, selectedTime);
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    private void setDateTimeFields(String dateTimeValue, DatePicker datePicker, ComboBox<String> timeBox) {
        if (dateTimeValue == null || dateTimeValue.isBlank()) {
            return;
        }

        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(dateTimeValue, DATE_TIME_FORMATTER);
            datePicker.setValue(parsedDateTime.toLocalDate());
            timeBox.setValue(parsedDateTime.toLocalTime().format(TIME_FORMATTER));
        } catch (DateTimeParseException ignored) {
        }
    }

    // SAMU: The overview is rebuilt after create, edit, assign, or delete.
    private void refreshView(String viewMode) {
        if ("ADMIN_EVENTS".equals(viewMode)) {
            showAdminDashboard("Events");
        } else if ("COORD_EVENTS".equals(viewMode)) {
            showCoordinatorDashboard("Events");
        } else if ("COORD_ACCESS".equals(viewMode)) {
            showCoordinatorDashboard("Manage Access");
        } else if ("CUSTOMER".equals(viewMode)) {
            showCustomerDashboard();
        }
    }

    // SAMU: The same delete button is reused in admin and coordinator event screens.
    private Button createDeleteEventButton(Event event, String viewMode) {
        Button deleteButton = new Button("Delete Event");
        deleteButton.getStyleClass().add("danger-btn");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setOnAction(e -> confirmDeleteEvent(event, viewMode));
        return deleteButton;
    }

    // SAMU: Delete asks for confirmation before changing the shared event list.
    private void confirmDeleteEvent(Event event, String viewMode) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(window);
        confirmation.initModality(Modality.WINDOW_MODAL);
        confirmation.setTitle("Delete Event");
        confirmation.setHeaderText("Delete \"" + event.getTitle() + "\"?");
        confirmation.setContentText("This event will be removed from every event overview.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteEvent(event, viewMode);
        }
    }

    // SAMU: If delete fails, the user gets an error message instead of a silent failure.
    private void deleteEvent(Event event, String viewMode) {
        boolean eventDeleted = eventController.deleteEvent(event);

        if (eventDeleted) {
            refreshView(viewMode);
        } else {
            showErrorMessage("Delete failed", "The selected event could not be deleted.");
        }
    }

    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(window);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
