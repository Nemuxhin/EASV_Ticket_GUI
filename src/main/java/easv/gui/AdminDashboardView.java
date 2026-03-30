package easv.gui;

import easv.be.Event;
import easv.be.User;
import easv.controller.EventController;
import easv.controller.UserController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminDashboardView {

    private final MainView mainView;
    private final EventController eventController;
    private final UserController userController;
    private final String activeTab;

    public AdminDashboardView(MainView mainView, EventController eventController,
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

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("\uD83D\uDEAA Logout");
        logoutBtn.getStyleClass().add("sidebar-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> mainView.showPortalSelection());

        sidebar.getChildren().addAll(logo, coordinatorsBtn, eventsBtn, spacer, logoutBtn);
        return sidebar;
    }

    private VBox createCoordinatorsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Manage Coordinators");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search coordinators...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(400);

        Button createBtn = new Button("\uFF0B Create Coordinator");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setPrefWidth(1000);
        createBtn.setOnAction(e -> showCreateCoordinatorDialog());

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

            Button deleteBtn = new Button("\uD83D\uDDD1 Delete");
            deleteBtn.getStyleClass().add("danger-btn");
            deleteBtn.setMaxWidth(Double.MAX_VALUE);
            deleteBtn.setOnAction(e -> {
                userController.deleteUser(user);
                mainView.showAdminDashboard("Coordinators");
            });

            card.getChildren().addAll(nameLbl, emailLbl, usernameLbl, new Separator(), deleteBtn);
            grid.getChildren().add(card);
        }

        content.getChildren().addAll(title, searchBar, createBtn, grid);
        return content;
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

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        for (Event event : eventController.getEvents()) {
            grid.getChildren().add(createEventCard(event));
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
        Label dateLbl = new Label("\uD83D\uDD52 " + event.getStartDateTime());
        dateLbl.getStyleClass().add("card-text");
        scheduleBox.getChildren().add(dateLbl);
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

        Button deleteBtn = new Button("\uD83D\uDDD1 Delete Event");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> {
            eventController.deleteEvent(event);
            mainView.showAdminDashboard("Events");
        });

        card.getChildren().addAll(
                top,
                scheduleBox,
                locationBox,
                notesHead,
                notesLbl,
                new Separator(),
                priceLbl,
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

    private void showCreateCoordinatorDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create Coordinator");
        dialog.setHeaderText("Enter coordinator details");
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Create", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Full name");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        form.getChildren().addAll(
                new Label("Full Name"), nameField,
                new Label("Username"), usernameField,
                new Label("Email"), emailField,
                new Label("Password"), passwordField
        );

        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return new User(
                        nameField.getText().trim(),
                        usernameField.getText().trim(),
                        passwordField.getText(),
                        emailField.getText().trim(),
                        "Event Coordinator"
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (user.getName().isBlank() || user.getUsername().isBlank() || user.getEmail().isBlank() || user.getPassword().isBlank()) {
                AlertHelper.showError("Invalid Coordinator", "Please fill in all coordinator fields.");
                return;
            }
            userController.createUser(user);
            mainView.showAdminDashboard("Coordinators");
        });
    }
}
