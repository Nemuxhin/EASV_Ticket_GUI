package easv.gui;

import easv.be.Event;
import easv.be.User;
import easv.controller.EventController;
import easv.controller.UserController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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
        BorderPane layout = new BorderPane();
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
                "👥 Coordinators",
                "Coordinators".equals(activeTab),
                e -> mainView.showAdminDashboard("Coordinators")
        );

        Button eventsBtn = createMenuBtn(
                "📅 Events",
                "Events".equals(activeTab),
                e -> mainView.showAdminDashboard("Events")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪 Logout");
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

        Button createBtn = new Button("＋ Create Coordinator");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setPrefWidth(1000);
        createBtn.setOnAction(e -> {
            int next = userController.getUsersByRole("Event Coordinator").size() + 1;

            User user = new User(
                    "New Coordinator " + next,
                    "coord" + next,
                    "1234",
                    "coord" + next + "@easv.dk",
                    "Event Coordinator"
            );

            userController.createUser(user);
            mainView.showAdminDashboard("Coordinators");
        });

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        List<User> coordinators = userController.getUsersByRole("Event Coordinator");

        for (User user : coordinators) {
            VBox card = new VBox(10);
            card.getStyleClass().add("event-card");

            Label nameLbl = new Label("👤 " + user.getName());
            nameLbl.getStyleClass().add("card-title");

            Label emailLbl = new Label("✉ " + user.getEmail());
            emailLbl.getStyleClass().add("card-text");

            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.getStyleClass().add("danger-btn");
            deleteBtn.setMaxWidth(Double.MAX_VALUE);
            deleteBtn.setOnAction(e -> {
                userController.deleteUser(user);
                mainView.showAdminDashboard("Coordinators");
            });

            card.getChildren().addAll(nameLbl, emailLbl, new Separator(), deleteBtn);
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

        Label dateLbl = new Label("🕒 " + event.getDate());
        dateLbl.getStyleClass().add("card-text");

        Label locationLbl = new Label("📍 " + event.getLocation());
        locationLbl.getStyleClass().add("card-text");

        Label notesHead = new Label("Notes");
        notesHead.getStyleClass().add("notes-head");

        Label notesLbl = new Label(event.getNotes());
        notesLbl.getStyleClass().add("card-text");

        Label priceLbl = new Label(event.getPrice());
        priceLbl.getStyleClass().add("price-text");

        Button deleteBtn = new Button("🗑 Delete Event");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> {
            eventController.deleteEvent(event);
            mainView.showAdminDashboard("Events");
        });

        card.getChildren().addAll(
                top,
                dateLbl,
                locationLbl,
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
}