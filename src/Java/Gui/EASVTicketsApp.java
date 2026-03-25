package Java.Gui;

import Java.Be.Event;
import Java.Controller.EventController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EASVTicketsApp extends Application {
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

        String[][] coordinators = {
                {"Sarah Jensen", "s.jensen@easv.dk"},
                {"Mikkel Andersen", "m.andersen@easv.dk"},
                {"Laura Nielsen", "l.nielsen@easv.dk"},
                {"Peter Christiansen", "p.chris@easv.dk"}
        };

        for (String[] coordinator : coordinators) {
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

        if ("CUSTOMER".equals(viewMode)) {
            Button buyButton = new Button("Buy Ticket");
            buyButton.getStyleClass().add("primary-btn");
            buyButton.setMaxWidth(Double.MAX_VALUE);
            buyButton.setOnAction(e -> showBuyTicket(event));

            card.getChildren().addAll(ViewFactory.createPriceLabel(event.getPrice()), buyButton);
        } else if ("ADMIN_EVENTS".equals(viewMode) || "COORD_EVENTS".equals(viewMode)) {
            card.getChildren().addAll(
                    ViewFactory.createPriceLabel(event.getPrice()),
                    createDeleteEventButton(event, viewMode)
            );
        } else if ("COORD_ACCESS".equals(viewMode)) {
            Label coordinatorTitle = new Label("Assigned Coordinators");
            coordinatorTitle.getStyleClass().add("notes-head");

            Button assignButton = new Button("Assign Access");
            assignButton.getStyleClass().add("primary-btn");
            assignButton.setMaxWidth(Double.MAX_VALUE);

            card.getChildren().addAll(
                    coordinatorTitle,
                    ViewFactory.createCoordinatorPillBox(event.getCoordinators()),
                    assignButton,
                    createDeleteEventButton(event, viewMode)
            );
        }

        return card;
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

    // SAMU: The form collects required and optional event fields.
    private void showCreateEventDialog(String viewMode) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.initOwner(window);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Create Event");
        dialog.setHeaderText("Fill in the event details");

        ButtonType saveButtonType = new ButtonType("Save Event", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextField startDateTimeField = new TextField();
        TextField endDateTimeField = new TextField();
        TextField locationField = new TextField();
        TextField locationGuidanceField = new TextField();
        TextArea notesField = new TextArea();
        notesField.setWrapText(true);
        notesField.setPrefRowCount(3);
        TextField priceField = new TextField();
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Available", "Selling Fast", "Sold Out");
        statusBox.setValue("Available");
        TextField coordinatorsField = new TextField();

        form.addRow(0, new Label("Title *"), titleField);
        form.addRow(1, new Label("Start date/time *"), startDateTimeField);
        form.addRow(2, new Label("End date/time"), endDateTimeField);
        form.addRow(3, new Label("Location *"), locationField);
        form.addRow(4, new Label("Location guidance"), locationGuidanceField);
        form.addRow(5, new Label("Notes *"), notesField);
        form.addRow(6, new Label("Price *"), priceField);
        form.addRow(7, new Label("Status"), statusBox);
        form.addRow(8, new Label("Coordinators"), coordinatorsField);

        dialog.getDialogPane().setContent(form);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String validationMessage = validateRequiredInput(
                    titleField.getText(),
                    startDateTimeField.getText(),
                    locationField.getText(),
                    notesField.getText(),
                    priceField.getText()
            );

            if (!validationMessage.isEmpty()) {
                e.consume();
                showErrorMessage("Missing required input", validationMessage);
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new Event(
                        titleField.getText().trim(),
                        startDateTimeField.getText().trim(),
                        endDateTimeField.getText().trim(),
                        locationField.getText().trim(),
                        locationGuidanceField.getText().trim(),
                        notesField.getText().trim(),
                        priceField.getText().trim(),
                        statusBox.getValue(),
                        parseCoordinators(coordinatorsField.getText())
                );
            }
            return null;
        });

        Optional<Event> result = dialog.showAndWait();
        if (result.isPresent()) {
            eventController.addEvent(result.get());
            refreshView(viewMode);
        }
    }

    // SAMU: Only required fields are checked here to keep the form logic simple.
    private String validateRequiredInput(String title, String startDateTime, String location, String notes, String price) {
        StringBuilder message = new StringBuilder();

        if (title == null || title.isBlank()) {
            message.append("- Title is required.\n");
        }
        if (startDateTime == null || startDateTime.isBlank()) {
            message.append("- Start date/time is required.\n");
        }
        if (location == null || location.isBlank()) {
            message.append("- Location is required.\n");
        }
        if (notes == null || notes.isBlank()) {
            message.append("- Notes are required.\n");
        }
        if (price == null || price.isBlank()) {
            message.append("- Price is required.");
        }

        return message.toString().trim();
    }

    // SAMU: Coordinators are entered as a simple comma-separated list.
    private String[] parseCoordinators(String coordinatorsText) {
        if (coordinatorsText == null || coordinatorsText.isBlank()) {
            return new String[0];
        }

        String[] rawCoordinators = coordinatorsText.split(",");
        List<String> cleanCoordinators = new ArrayList<>();

        for (String coordinator : rawCoordinators) {
            String cleanValue = coordinator.trim();
            if (!cleanValue.isEmpty()) {
                cleanCoordinators.add(cleanValue);
            }
        }

        return cleanCoordinators.toArray(new String[0]);
    }

    // SAMU: The overview is rebuilt after create or delete so the UI stays in sync.
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
