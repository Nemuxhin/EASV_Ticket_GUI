package easv.gui;

import easv.be.Event;
import easv.be.User;
import easv.controller.EventController;
import easv.controller.TicketController;
import easv.controller.UserController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardView {

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
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-bg");

        VBox shell = new VBox();
        shell.getChildren().addAll(createTopHeader(), createTopNavigation());

        VBox content = switch (activeTab) {
            case "Events" -> createEventsContent();
            case "Manage Access" -> createManageAccessContent();
            case "Create User", "Create Coordinator" -> createUserCreateContent();
            case "Users", "Coordinators" -> createUsersContent();
            default -> createUsersContent();
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

        Label title = new Label("EASV Tickets - Admin Portal");
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
                createTopNavButton("Users", "Users".equals(activeTab) || "Coordinators".equals(activeTab)),
                createTopNavButton("Create User", "Create User".equals(activeTab) || "Create Coordinator".equals(activeTab)),
                createTopNavButton("Manage Access", "Manage Access".equals(activeTab)),
                createTopNavButton("Events", "Events".equals(activeTab))
        );

        Separator divider = new Separator();
        divider.getStyleClass().add("portal-tab-divider");

        wrapper.getChildren().addAll(nav, divider);
        return wrapper;
    }

    private Button createTopNavButton(String label, boolean active) {
        Button button = new Button(label);
        button.getStyleClass().add(active ? "portal-tab-btn-active" : "portal-tab-btn");

        switch (label) {
            case "Users" -> button.setOnAction(e -> mainView.showAdminDashboard("Users"));
            case "Create User" -> button.setOnAction(e -> mainView.showAdminDashboard("Create User"));
            case "Manage Access" -> button.setOnAction(e -> mainView.showAdminDashboard("Manage Access"));
            case "Events" -> button.setOnAction(e -> mainView.showAdminDashboard("Events"));
            default -> {
            }
        }

        return button;
    }

    private VBox createUsersContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 34, 34, 34));
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);
        showUserList(content);
        return content;
    }

    private void showUserList(VBox content) {
        content.getChildren().clear();

        Label title = new Label("Users");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search users by name, username, email, or role...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("All Roles", "Admin", "Event Coordinator");
        roleFilter.setValue("All Roles");
        roleFilter.getStyleClass().add("dashboard-select");
        roleFilter.setPrefWidth(220);
        roleFilter.setPrefHeight(46);

        HBox filters = new HBox(16, searchBar, roleFilter);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBar, Priority.ALWAYS);

        GridPane grid = createTwoColumnGrid();
        List<User> allUsers = userController.getAllUsers();

        Runnable render = () -> {
            List<User> filteredUsers = new ArrayList<>();

            for (User user : allUsers) {
                if (!matchesUserSearch(user, searchBar.getText())) {
                    continue;
                }
                if (!matchesRoleFilter(user, roleFilter.getValue())) {
                    continue;
                }
                filteredUsers.add(user);
            }

            renderUserCards(grid, filteredUsers, content);
        };

        searchBar.textProperty().addListener((obs, oldValue, newValue) -> render.run());
        roleFilter.valueProperty().addListener((obs, oldValue, newValue) -> render.run());
        render.run();

        content.getChildren().addAll(title, filters, grid);
    }

    private void renderUserCards(GridPane grid, List<User> users, VBox content) {
        grid.getChildren().clear();

        if (users.isEmpty()) {
            Label empty = new Label("No users match your search.");
            empty.getStyleClass().add("card-text");
            grid.add(empty, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;

        for (User user : users) {
            VBox card = createUserCard(content, user);
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setFillWidth(card, true);

            grid.add(card, column, row);

            column++;
            if (column == 2) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createUserCard(VBox content, User user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(250);

        Label nameLbl = new Label("\uD83D\uDC64 " + safeText(user.getName()));
        nameLbl.getStyleClass().add("card-title");

        Label roleLbl = new Label("Role: " + safeText(user.getRole()));
        roleLbl.getStyleClass().add("card-text");

        Label emailLbl = new Label("\u2709 " + safeText(user.getEmail()));
        emailLbl.getStyleClass().add("card-text");

        Label usernameLbl = new Label("Username: " + safeText(user.getUsername()));
        usernameLbl.getStyleClass().add("card-text");

        Button editBtn = new Button("Edit User");
        editBtn.getStyleClass().add("secondary-btn");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.setOnAction(e -> showUserEditForm(content, user));

        Button deleteBtn = new Button("\uD83D\uDDD1 Delete User");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);

        if (isCurrentLoggedInUser(user)) {
            deleteBtn.setDisable(true);
            deleteBtn.setText("Current Logged-in User");
        } else {
            deleteBtn.setOnAction(e -> {
                boolean confirmed = AlertHelper.showConfirmation(
                        "Delete User",
                        "Are you sure you want to delete this user?"
                );
                if (!confirmed) {
                    return;
                }

                userController.deleteUser(user);
                showUserList(content);
            });
        }

        Region buttonSpacer = new Region();
        VBox.setVgrow(buttonSpacer, Priority.ALWAYS);

        card.getChildren().addAll(
                nameLbl,
                roleLbl,
                emailLbl,
                usernameLbl,
                buttonSpacer,
                new Separator(),
                editBtn,
                deleteBtn
        );

        return card;
    }

    private VBox createUserCreateContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 34, 34, 34));
        showUserCreateForm(content);
        return content;
    }

    private void showUserCreateForm(VBox content) {
        content.getChildren().clear();

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Create New User");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("\u2190 Back");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setOnAction(e -> mainView.showAdminDashboard("Users"));

        topBar.getChildren().addAll(title, spacer, backBtn);

        VBox formCard = createUserCreateFormCard(() -> mainView.showAdminDashboard("Users"));
        content.getChildren().addAll(topBar, formCard);
    }

    private void showUserEditForm(VBox content, User user) {
        content.getChildren().clear();

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Edit User");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("\u2190 Back");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setOnAction(e -> showUserList(content));

        topBar.getChildren().addAll(title, spacer, backBtn);

        VBox formCard = createUserEditFormCard(user, () -> showUserList(content));
        content.getChildren().addAll(topBar, formCard);
    }

    private VBox createManageAccessContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(30, 34, 34, 34));

        Label title = new Label("Manage Coordinator Access");
        title.getStyleClass().add("page-title");

        VBox list = new VBox(16);
        List<User> coordinators = userController.getUsersByRole("Event Coordinator");

        for (Event event : eventController.getEvents()) {
            list.getChildren().add(createManageAccessCard(event, coordinators));
        }

        content.getChildren().addAll(title, list);
        return content;
    }

    private VBox createManageAccessCard(Event event, List<User> coordinators) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("event-card", "event-list-card");

        Label title = new Label(safeText(event.getTitle()));
        title.getStyleClass().add("card-title");

        String startDateTime = event.getStartDateTime() == null ? "" : event.getStartDateTime();
        Label date = new Label(startDateTime.replace(" at ", ", "));
        date.getStyleClass().add("card-text");

        Label assigned = new Label("Assigned Coordinators:");
        assigned.getStyleClass().add("notes-head");

        FlowPane pills = new FlowPane(8, 8);
        List<String> selected = new ArrayList<>();
        if (event.getCoordinators() != null) {
            selected.addAll(Arrays.asList(event.getCoordinators()));
        }

        for (User coordinator : coordinators) {
            String name = coordinator.getName();
            Button pill = new Button(name);
            pill.getStyleClass().add("assign-pill");

            if (selected.contains(name)) {
                pill.getStyleClass().add("assign-pill-selected");
            }

            pill.setOnAction(e -> {
                if (selected.contains(name)) {
                    selected.remove(name);
                    pill.getStyleClass().remove("assign-pill-selected");
                } else {
                    selected.add(name);
                    if (!pill.getStyleClass().contains("assign-pill-selected")) {
                        pill.getStyleClass().add("assign-pill-selected");
                    }
                }

                eventController.setCoordinators(event, selected.toArray(new String[0]));
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

    private VBox createEventsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 34, 34, 34));
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("Manage Events");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search events...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = createTwoColumnGrid();
        List<Event> allEvents = eventController.getEvents();

        Runnable render = () -> {
            List<Event> filteredEvents = new ArrayList<>();

            for (Event event : allEvents) {
                if (!matchesEventSearch(event, searchBar.getText())) {
                    continue;
                }
                filteredEvents.add(event);
            }

            renderEventCards(grid, filteredEvents);
        };

        searchBar.textProperty().addListener((obs, oldValue, newValue) -> render.run());
        render.run();

        content.getChildren().addAll(title, searchBar, grid);
        return content;
    }

    private void renderEventCards(GridPane grid, List<Event> events) {
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
            GridPane.setFillWidth(card, true);

            grid.add(card, column, row);

            column++;
            if (column == 2) {
                column = 0;
                row++;
            }
        }
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

    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(340);

        HBox top = new HBox(12);
        top.setAlignment(Pos.TOP_LEFT);

        Label titleLbl = new Label(safeText(event.getTitle()));
        titleLbl.getStyleClass().add("card-title");
        titleLbl.setWrapText(true);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        String status = ticketController.getEventStatus(event);
        Label statusLbl = new Label(status);
        statusLbl.getStyleClass().add(statusStyleClass(status));

        top.getChildren().addAll(titleLbl, statusLbl);

        VBox scheduleBox = new VBox(6);
        Label dateLbl = new Label("\uD83D\uDD52 " + safeText(event.getStartDateTime()));
        dateLbl.getStyleClass().add("card-text");
        scheduleBox.getChildren().add(dateLbl);

        if (event.hasEndDateTime()) {
            Label endLbl = new Label("Ends: " + safeText(event.getEndDateTime()));
            endLbl.getStyleClass().add("card-text");
            endLbl.setWrapText(true);
            scheduleBox.getChildren().add(endLbl);
        }

        VBox locationBox = new VBox(6);
        Label locationLbl = new Label("\uD83D\uDCCD " + safeText(event.getLocation()));
        locationLbl.getStyleClass().add("card-text");
        locationLbl.setWrapText(true);
        locationBox.getChildren().add(locationLbl);

        if (event.hasLocationGuidance()) {
            Label guidanceLbl = new Label("Guidance: " + safeText(event.getLocationGuidance()));
            guidanceLbl.getStyleClass().add("card-text");
            guidanceLbl.setWrapText(true);
            locationBox.getChildren().add(guidanceLbl);
        }

        Label notesHead = new Label("Notes");
        notesHead.getStyleClass().add("notes-head");

        Label notesLbl = new Label(safeText(event.getNotes()));
        notesLbl.getStyleClass().add("card-text");
        notesLbl.setWrapText(true);

        Label priceLbl = new Label(safeText(event.getPrice()));
        priceLbl.getStyleClass().add("price-text");

        // (Samu) Show the ticket types directly on the event card.
        Label ticketTypesHead = new Label("Ticket Types");
        ticketTypesHead.getStyleClass().add("notes-head");
        Label ticketTypesSummary = buildTicketTypeSummaryLabel(ticketController.getTicketTypePricesForEvent(event));

        Region buttonSpacer = new Region();
        VBox.setVgrow(buttonSpacer, Priority.ALWAYS);

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
            mainView.showAdminDashboard("Events");
        });

        card.getChildren().addAll(
                top,
                scheduleBox,
                locationBox,
                notesHead,
                notesLbl,
                buttonSpacer,
                new Separator(),
                priceLbl,
                ticketTypesHead,
                ticketTypesSummary,
                deleteBtn
        );

        return card;
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

    private VBox createUserCreateFormCard(Runnable onDoneOrCancel) {
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("event-card");
        formCard.setMaxWidth(Double.MAX_VALUE);

        Label heading = new Label("Create New User");
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

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Admin", "Event Coordinator");
        roleBox.setValue("Event Coordinator");
        roleBox.getStyleClass().add("dashboard-select");
        roleBox.setPrefHeight(46);
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Button createBtn = new Button("Create User");
        createBtn.getStyleClass().add("primary-btn");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");
        cancelBtn.setOnAction(e -> onDoneOrCancel.run());

        createBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String role = roleBox.getValue();

            String validation = userController.validateUserInput(name, email, username, password, role);
            if (validation != null) {
                AlertHelper.showError("Invalid User", validation);
                return;
            }

            User user = new User(name, username, password, email, role);
            userController.createUser(user);
            AlertHelper.showInfo("User Created", "The user was created successfully.");
            onDoneOrCancel.run();
        });

        HBox actions = new HBox(12, createBtn, cancelBtn);

        formCard.getChildren().addAll(
                heading,
                fieldBox("Full Name *", nameField),
                fieldBox("Email *", emailField),
                fieldBox("Username *", usernameField),
                passwordFieldBox("Password *", passwordField, visiblePasswordField),
                fieldBox("Role *", roleBox),
                actions
        );

        return formCard;
    }

    private VBox createUserEditFormCard(User user, Runnable onDoneOrCancel) {
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("event-card");
        formCard.setMaxWidth(Double.MAX_VALUE);

        Label heading = new Label("Edit User");
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

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Admin", "Event Coordinator");
        roleBox.setValue(user.getRole());
        roleBox.getStyleClass().add("dashboard-select");
        roleBox.setPrefHeight(46);
        roleBox.setMaxWidth(Double.MAX_VALUE);

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
            String role = roleBox.getValue();

            String validation = userController.validateUserUpdateInput(name, email, username, password, role, user.getUsername());
            if (validation != null) {
                AlertHelper.showError("Invalid User", validation);
                return;
            }

            userController.updateUser(user, name, email, username, password, role);
            AlertHelper.showInfo("User Updated", "The user changes were saved successfully.");
            onDoneOrCancel.run();
        });

        HBox actions = new HBox(12, saveBtn, cancelBtn);

        formCard.getChildren().addAll(
                heading,
                fieldBox("Full Name *", nameField),
                fieldBox("Email *", emailField),
                fieldBox("Username *", usernameField),
                passwordFieldBox("Password *", passwordField, visiblePasswordField),
                fieldBox("Role *", roleBox),
                actions
        );

        return formCard;
    }

    private VBox fieldBox(String labelText, javafx.scene.Node field) {
        VBox form = new VBox(10);
        form.setFillWidth(true);
        form.setMaxWidth(Double.MAX_VALUE);

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

        Button toggleBtn = new Button("Show");
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

            toggleBtn.setText(showing ? "Show" : "Hide");
        });

        return fieldBox(labelText, stack);
    }

    private boolean matchesUserSearch(User user, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String needle = query.trim().toLowerCase();
        return safeText(user.getName()).toLowerCase().contains(needle)
                || safeText(user.getUsername()).toLowerCase().contains(needle)
                || safeText(user.getEmail()).toLowerCase().contains(needle)
                || safeText(user.getRole()).toLowerCase().contains(needle);
    }

    private boolean matchesRoleFilter(User user, String filter) {
        if (filter == null || filter.isBlank() || "All Roles".equalsIgnoreCase(filter)) {
            return true;
        }

        return safeText(user.getRole()).equalsIgnoreCase(filter);
    }

    private boolean matchesEventSearch(Event event, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String needle = query.trim().toLowerCase();
        return safeText(event.getTitle()).toLowerCase().contains(needle)
                || safeText(event.getLocation()).toLowerCase().contains(needle)
                || safeText(event.getStatus()).toLowerCase().contains(needle);
    }

    private boolean isCurrentLoggedInUser(User user) {
        User currentUser = mainView.getCurrentUser();
        if (currentUser == null || user == null) {
            return false;
        }

        return safeText(currentUser.getUsername()).equalsIgnoreCase(safeText(user.getUsername()))
                && safeText(currentUser.getRole()).equalsIgnoreCase(safeText(user.getRole()));
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String statusStyleClass(String status) {
        return switch (status) {
            case "Sold Out" -> "status-sold";
            case "Fast Selling", "Selling Fast" -> "status-fast";
            default -> "status-avail";
        };
    }
}
