package Java.Gui;

import Java.Be.Event;
import Java.Be.User;
import Java.gui.controller.EventController;
import Java.gui.controller.UserController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class CoordinatorDashboardView {

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
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar());

        VBox content = "Manage Access".equals(activeTab)
                ? createManageAccessContent()
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
                "📅 Events",
                "Events".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Events")
        );

        Button accessBtn = createMenuBtn(
                "👥 Manage Access",
                "Manage Access".equals(activeTab),
                e -> mainView.showCoordinatorDashboard("Manage Access")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().add("sidebar-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e ->  mainView.showPortalSelection());

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

        Button createBtn = new Button("＋ Create Event");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setPrefWidth(1000);
        createBtn.setOnAction(e -> {
            int next = eventController.getEvents().size() + 1;

            Event event = new Event(
                    "New Event " + next,
                    "01 Jan 2027 at 12:00",
                    "EASV Campus, Esbjerg",
                    "New event created from coordinator dashboard",
                    "100 DKK",
                    "Available",
                    new String[]{}
            );

            eventController.createEvent(event);
            mainView.showCoordinatorDashboard("Events");
        });

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
            mainView.showCoordinatorDashboard("Events");
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

        Label dateLbl = new Label("🕒 " + event.getDate());
        dateLbl.getStyleClass().add("card-text");

        Label locationLbl = new Label("📍 " + event.getLocation());
        locationLbl.getStyleClass().add("card-text");

        Label assignedHead = new Label("Assigned Coordinators");
        assignedHead.getStyleClass().add("notes-head");

        FlowPane pillBox = new FlowPane(5, 5);
        for (String coordinator : event.getCoordinators()) {
            Label pill = new Label(coordinator);
            pill.getStyleClass().add("coord-pill");
            pillBox.getChildren().add(pill);
        }

        Button assignBtn = new Button("👥 Assign Access");
        assignBtn.getStyleClass().add("primary-btn");
        assignBtn.setMaxWidth(Double.MAX_VALUE);
        assignBtn.setOnAction(e -> {
            List<User> coordinators = userController.getUsersByRole("Event Coordinator");

            for (User user : coordinators) {
                boolean alreadyAssigned = false;

                for (String assigned : event.getCoordinators()) {
                    if (assigned.equalsIgnoreCase(user.getName())) {
                        alreadyAssigned = true;
                        break;
                    }
                }

                if (!alreadyAssigned) {
                    eventController.assignCoordinator(event, user.getName());
                    mainView.showCoordinatorDashboard("Manage Access");
                    return;
                }
            }

            showInfo("All available coordinators are already assigned.");
        });

        Button deleteBtn = new Button("🗑 Delete Event");
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Manage Access");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}