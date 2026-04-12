package easv.gui;

import easv.be.Event;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AdminDashboardView {
    private static final String ALL_COORDINATORS = "All Coordinators";

    private final MainView mainView;
    private final EventController eventController;
    private final TicketController ticketController;
    private final UserController userController;
    private final String activeTab;

    public AdminDashboardView(MainView mainView, EventController eventController,
                              UserController userController, String activeTab) {
        this.mainView = mainView;
        this.eventController = eventController;
        this.ticketController = new TicketController();
        this.userController = userController;
        this.activeTab = activeTab;
    }

    public Parent getView() {
        javafx.scene.layout.BorderPane layout = new javafx.scene.layout.BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar());

        VBox content = "Events".equals(activeTab)
                ? createEventsContent()
                : createCoordinatorsContent();

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

        Label logo = new Label("Admin Portal");
        logo.getStyleClass().add("sidebar-logo");

        Button coordinatorsBtn = createMenuBtn(
                "\uD83D\uDC65 Coordinators",
                "Coordinators".equals(activeTab),
                e -> mainView.showAdminDashboard("Coordinators")
        );

        Button eventsBtn = createMenuBtn(
                "\uD83D\uDCC5 Events",
                "Events".equals(activeTab),
                e -> mainView.showAdminDashboard("Events")
        );

        Button soldTicketsBtn = createMenuBtn(
                "Sold Tickets",
                false,
                e -> mainView.showSoldTickets("Admin")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("\uD83D\uDEAA Logout");
        logoutBtn.getStyleClass().add("sidebar-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> mainView.showPortalSelection());

        sidebar.getChildren().addAll(logo, coordinatorsBtn, eventsBtn, soldTicketsBtn, spacer, logoutBtn);
        return sidebar;
    }

    private VBox createCoordinatorsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));
        showCoordinatorList(content);
        return content;
    }

    private void showCoordinatorList(VBox content) {
        content.getChildren().clear();

        Label title = new Label("Event Coordinators");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search coordinators...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(400);

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button createBtn = new Button("+ Create Coordinator");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setOnAction(e -> showCoordinatorCreateForm(content));

        topBar.getChildren().addAll(title, topSpacer, createBtn);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        List<User> coordinators = userController.getUsersByRole("Event Coordinator");

        for (User user : coordinators) {
            VBox card = new VBox(10);
            card.getStyleClass().add("event-card");

            Label nameLbl = new Label("\uD83D\uDC64 " + user.getName());
            nameLbl.getStyleClass().add("card-title");

            Label emailLbl = new Label("\u2709 " + user.getEmail());
            emailLbl.getStyleClass().add("card-text");

            Label usernameLbl = new Label("Username: " + user.getUsername());
            usernameLbl.getStyleClass().add("card-text");

            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("secondary-btn");
            editBtn.setMaxWidth(Double.MAX_VALUE);
            editBtn.setOnAction(e -> showCoordinatorEditForm(content, user));

            Button deleteBtn = new Button("\uD83D\uDDD1 Delete");
            deleteBtn.getStyleClass().add("danger-btn");
            deleteBtn.setMaxWidth(Double.MAX_VALUE);
            deleteBtn.setOnAction(e -> {
                userController.deleteUser(user);
                showCoordinatorList(content);
            });

            card.getChildren().addAll(
                    nameLbl,
                    emailLbl,
                    usernameLbl,
                    new Separator(),
                    editBtn,
                    deleteBtn
            );
            grid.getChildren().add(card);
        }

        content.getChildren().addAll(topBar, searchBar, grid);
    }

    private void showCoordinatorCreateForm(VBox content) {
        content.getChildren().clear();

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Create New Coordinator");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("\u2190 Back");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setOnAction(e -> showCoordinatorList(content));

        topBar.getChildren().addAll(title, spacer, backBtn);

        VBox formCard = createCoordinatorCreateFormCard(() -> showCoordinatorList(content));
        content.getChildren().addAll(topBar, formCard);
    }

    private void showCoordinatorEditForm(VBox content, User user) {
        content.getChildren().clear();

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Edit Coordinator");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("\u2190 Back");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setOnAction(e -> showCoordinatorList(content));

        topBar.getChildren().addAll(title, spacer, backBtn);

        VBox formCard = createCoordinatorEditFormCard(user, () -> showCoordinatorList(content));
        content.getChildren().addAll(topBar, formCard);
    }

    private VBox createEventsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Manage Events");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(400);

        ComboBox<String> coordinatorFilter = createCoordinatorFilterBox();

        HBox toolbar = new HBox(12, searchBar, coordinatorFilter);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        Runnable refreshGrid = () -> populateEventGrid(grid, searchBar.getText(), coordinatorFilter.getValue());
        searchBar.textProperty().addListener((obs, oldValue, newValue) -> refreshGrid.run());
        coordinatorFilter.valueProperty().addListener((obs, oldValue, newValue) -> refreshGrid.run());
        refreshGrid.run();

        content.getChildren().addAll(title, toolbar, grid);
        return content;
    }

    // (Samu) Admin can filter the event list by assigned coordinator.
    private void populateEventGrid(FlowPane grid, String searchText, String coordinatorName) {
        grid.getChildren().clear();

        for (Event event : eventController.getEvents()) {
            if (eventMatchesFilters(event, searchText, coordinatorName)) {
                grid.getChildren().add(createEventCard(event));
            }
        }
    }

    private ComboBox<String> createCoordinatorFilterBox() {
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.setItems(FXCollections.observableArrayList(getCoordinatorFilterValues()));
        filterBox.setValue(ALL_COORDINATORS);
        filterBox.setPrefWidth(220);
        filterBox.getStyleClass().add("input-field");
        return filterBox;
    }

    private List<String> getCoordinatorFilterValues() {
        List<String> values = new ArrayList<>();
        values.add(ALL_COORDINATORS);

        for (User user : userController.getUsersByRole("Event Coordinator")) {
            values.add(user.getName());
        }

        return values;
    }

    private boolean eventMatchesFilters(Event event, String searchText, String coordinatorName) {
        return matchesSearch(event, searchText) && matchesCoordinator(event, coordinatorName);
    }

    private boolean matchesSearch(Event event, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return true;
        }

        String value = searchText.trim().toLowerCase(Locale.ENGLISH);
        return event.getTitle().toLowerCase(Locale.ENGLISH).contains(value)
                || event.getLocation().toLowerCase(Locale.ENGLISH).contains(value);
    }

    private boolean matchesCoordinator(Event event, String coordinatorName) {
        if (coordinatorName == null || coordinatorName.isBlank() || ALL_COORDINATORS.equals(coordinatorName)) {
            return true;
        }

        for (String coordinator : event.getCoordinators()) {
            if (coordinatorName.equals(coordinator)) {
                return true;
            }
        }

        return false;
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
        Label dateLbl = new Label("\uD83D\uDD52 " + event.getStartDateTime());
        dateLbl.getStyleClass().add("date-text");
        scheduleBox.getChildren().add(dateLbl);
        if (event.hasEndDateTime()) {
            Label endLbl = new Label("Ends: " + event.getEndDateTime());
            endLbl.getStyleClass().add("date-text");
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

        VBox ticketTypesBox = createTicketTypesBox(event);

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
        deleteBtn.setOnAction(e -> confirmDeleteEvent(event));

        card.getChildren().addAll(
                top,
                scheduleBox,
                locationBox,
                notesHead,
                notesLbl,
                new Separator(),
                priceLbl,
                ticketTypesBox,
                assignedHead,
                pillBox,
                assignBtn,
                deleteBtn
        );

        return card;
    }

    // (Samu) Admin sees the ticket types before deciding what to do with the event.
    private VBox createTicketTypesBox(Event event) {
        VBox box = new VBox(8);

        Label heading = new Label("Ticket Types");
        heading.getStyleClass().add("notes-head");

        FlowPane pills = new FlowPane(6, 6);
        LinkedHashMap<String, String> ticketTypes = ticketController.getTicketTypePricesForEvent(event);

        for (Map.Entry<String, String> entry : ticketTypes.entrySet()) {
            Label pill = new Label(entry.getKey() + " - " + entry.getValue());
            pill.getStyleClass().add("ticket-option-pill");
            pills.getChildren().add(pill);
        }

        box.getChildren().addAll(heading, pills);
        return box;
    }

    // (Samu) Admin gets a confirmation dialog and an error if the delete does not work.
    private void confirmDeleteEvent(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Event");
        alert.setHeaderText(null);
        alert.setContentText("Delete \"" + event.getTitle() + "\"?");

        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean deleted = eventController.deleteEvent(event);
            if (!deleted) {
                AlertHelper.showError("Delete Failed", "The selected event could not be deleted.");
                return;
            }

            mainView.showAdminDashboard("Events");
        } catch (RuntimeException ex) {
            AlertHelper.showError("Delete Failed", ex.getMessage());
        }
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

    private VBox createCoordinatorCreateFormCard(Runnable onDoneOrCancel) {
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("event-card");
        formCard.setMaxWidth(Double.MAX_VALUE);

        Label heading = new Label("Create New Coordinator");
        heading.getStyleClass().add("card-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");
        nameField.getStyleClass().add("input-field");

        TextField emailField = new TextField();
        emailField.setPromptText("email@easv.dk");
        emailField.getStyleClass().add("input-field");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.getStyleClass().add("input-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter password");

        Button createBtn = new Button("Create Coordinator");
        createBtn.getStyleClass().add("primary-btn");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");
        cancelBtn.setOnAction(e -> onDoneOrCancel.run());

        createBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            String validation = userController.validateCoordinatorInput(name, email, username, password);
            if (validation != null) {
                AlertHelper.showError("Invalid Coordinator", validation);
                return;
            }

            User user = new User(
                    name,
                    username,
                    password,
                    email,
                    "Event Coordinator"
            );

            userController.createUser(user);
            onDoneOrCancel.run();
        });

        HBox actions = new HBox(12, createBtn, cancelBtn);

        formCard.getChildren().addAll(
                heading,
                fieldBox("Full Name *", nameField),
                fieldBox("Email *", emailField),
                fieldBox("Username *", usernameField),
                passwordFieldBox("Password *", passwordField, visiblePasswordField),
                actions
        );

        return formCard;
    }

    private VBox createCoordinatorEditFormCard(User user, Runnable onDoneOrCancel) {
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("event-card");
        formCard.setMaxWidth(Double.MAX_VALUE);

        Label heading = new Label("Edit Coordinator");
        heading.getStyleClass().add("card-title");

        TextField nameField = new TextField(user.getName());
        nameField.getStyleClass().add("input-field");

        TextField emailField = new TextField(user.getEmail());
        emailField.getStyleClass().add("input-field");

        TextField usernameField = new TextField(user.getUsername());
        usernameField.getStyleClass().add("input-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setText(user.getPassword());

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setText(user.getPassword());

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("primary-btn");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");
        cancelBtn.setOnAction(e -> onDoneOrCancel.run());

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            String validation = userController.validateCoordinatorInput(name, email, username, password);
            if (validation != null) {
                AlertHelper.showError("Invalid Coordinator", validation);
                return;
            }

            userController.updateUser(user, name, email, username, password);
            onDoneOrCancel.run();
        });

        HBox actions = new HBox(12, saveBtn, cancelBtn);

        formCard.getChildren().addAll(
                heading,
                fieldBox("Full Name *", nameField),
                fieldBox("Email *", emailField),
                fieldBox("Username *", usernameField),
                passwordFieldBox("Password *", passwordField, visiblePasswordField),
                actions
        );

        return formCard;
    }

    private VBox fieldBox(String labelText, javafx.scene.Node field) {
        VBox form = new VBox(10);
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        form.getChildren().addAll(label, field);
        return form;
    }

    private VBox passwordFieldBox(String labelText, PasswordField passwordField, TextField visibleField) {
        passwordField.getStyleClass().add("input-field");
        visibleField.getStyleClass().add("input-field");

        visibleField.setManaged(false);
        visibleField.setVisible(false);

        passwordField.textProperty().bindBidirectional(visibleField.textProperty());

        Button toggleBtn = new Button("👁");
        toggleBtn.getStyleClass().add("password-toggle-inside-btn");
        toggleBtn.setFocusTraversable(false);

        StackPane stack = new StackPane(passwordField, visibleField, toggleBtn);
        StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleBtn, new Insets(0, 10, 0, 0));

        toggleBtn.setOnAction(e -> {
            boolean showing = visibleField.isVisible();

            visibleField.setVisible(!showing);
            visibleField.setManaged(!showing);

            passwordField.setVisible(showing);
            passwordField.setManaged(showing);

            toggleBtn.setText(showing ? "👁" : "🙈");
        });

        return fieldBox(labelText, stack);
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
            mainView.showAdminDashboard("Events");
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
        alert.setTitle("Assign Coordinators");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
