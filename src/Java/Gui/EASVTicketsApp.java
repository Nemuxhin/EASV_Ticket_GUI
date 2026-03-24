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
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class EASVTicketsApp extends Application {

    private final EventController eventController = new EventController();
    private final StackPane rootPane = new StackPane();

    // Dummy coordinators for UI
    private final List<Coordinator> coordinators = Arrays.asList(
            new Coordinator("Lars Hansen", "lars.hansen@easv.dk", "+45 12 34 56 78", "Event Coordinator", "Active"),
            new Coordinator("Maria Petersen", "maria.petersen@easv.dk", "+45 23 45 67 89", "Event Coordinator", "Active"),
            new Coordinator("Thomas Nielsen", "thomas.nielsen@easv.dk", "+45 34 56 78 90", "Event Coordinator", "Active")
    );

    @Override
    public void start(Stage primaryStage) {
        showPortalSelection();

        Scene mainScene = new Scene(rootPane, 1400, 900);
        mainScene.getStylesheets().add(
                getClass().getResource("/CSS/easv-style.css").toExternalForm()
        );

        primaryStage.setTitle("EASV Ticket Management System");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    // =========================================================
    // PORTAL SELECTION
    // =========================================================

    private void showPortalSelection() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-bg");

        VBox header = ViewFactory.createHeader(
                "EASV Tickets",
                "Management System",
                "Select Portal",
                "Choose Access"
        );

        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER);

        VBox admin = ViewFactory.createPortalCard("👤", "Admin", "Manage Coordinators");
        admin.setOnMouseClicked(e -> showLogin("Admin"));

        VBox coordinator = ViewFactory.createPortalCard("🗓", "Event Coordinator", "Manage Events");
        coordinator.setOnMouseClicked(e -> showLogin("Coordinator"));

        cards.getChildren().addAll(admin, coordinator);
        layout.getChildren().addAll(header, cards);

        rootPane.getChildren().setAll(layout);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void showLogin(String role) {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-bg");

        VBox header = ViewFactory.createHeader(
                "EASV Tickets - " + role + " Portal",
                "",
                "Login",
                "Enter your credentials"
        );

        VBox formBox = new VBox(15);
        formBox.setMaxWidth(380);
        formBox.getStyleClass().add("login-form");
        formBox.setPadding(new Insets(30));

        Label userLbl = new Label("Username *");
        userLbl.getStyleClass().add("form-label");

        TextField userField = new TextField();
        userField.getStyleClass().add("input-field");

        Label passLbl = new Label("Password *");
        passLbl.getStyleClass().add("form-label");

        PasswordField passField = new PasswordField();
        passField.getStyleClass().add("input-field");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("primary-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            if ("Admin".equals(role)) {
                showAdminCoordinatorsPage();
            } else {
                showCoordinatorEventsPage();
            }
        });

        Button backBtn = new Button("🏠 Back to Portal Selection");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> showPortalSelection());

        formBox.getChildren().addAll(
                userLbl, userField,
                passLbl, passField,
                loginBtn,
                new Separator(),
                backBtn
        );

        layout.getChildren().addAll(header, formBox);
        rootPane.getChildren().setAll(layout);
    }

    // =========================================================
    // ADMIN PAGES
    // =========================================================

    private void showAdminCoordinatorsPage() {
        BorderPane layout = createTopShell("EASV Tickets - Admin Portal", () -> showPortalSelection());

        VBox content = new VBox(25);
        content.setPadding(new Insets(28));

        HBox tabs = ViewFactory.createTabs(
                new TabItem("Coordinators", true, this::showAdminCoordinatorsPage),
                new TabItem("Manage Access", false, this::showAdminAccessPage)
        );

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Event Coordinators");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = new Button("+  Create Coordinator");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setOnAction(e -> showCreateCoordinatorPage());

        headerRow.getChildren().addAll(title, spacer, createBtn);

        FlowPane cards = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        for (Coordinator c : coordinators) {
            cards.getChildren().add(
                    ViewFactory.createCoordinatorCard(
                            c.name,
                            c.email,
                            c.phone,
                            c.role,
                            c.status,
                            "Delete",
                            null
                    )
            );
        }

        content.getChildren().addAll(tabs, headerRow, cards);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void showCreateCoordinatorPage() {
        BorderPane layout = createTopShell("EASV Tickets - Admin Portal", () -> showPortalSelection());

        VBox content = new VBox(25);
        content.setPadding(new Insets(28));

        HBox tabs = ViewFactory.createTabs(
                new TabItem("Coordinators", true, this::showAdminCoordinatorsPage),
                new TabItem("Manage Access", false, this::showAdminAccessPage)
        );

        Label title = new Label("Event Coordinators");
        title.getStyleClass().add("page-title");

        VBox form = ViewFactory.createCoordinatorForm(
                "Create New Coordinator",
                this::showAdminCoordinatorsPage,
                this::showAdminCoordinatorsPage
        );

        content.getChildren().addAll(tabs, title, form);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void showAdminAccessPage() {
        BorderPane layout = createTopShell("EASV Tickets - Admin Portal", () -> showPortalSelection());

        VBox content = new VBox(25);
        content.setPadding(new Insets(28));

        HBox tabs = ViewFactory.createTabs(
                new TabItem("Coordinators", false, this::showAdminCoordinatorsPage),
                new TabItem("Manage Access", true, this::showAdminAccessPage)
        );

        Label title = new Label("Manage Coordinator Access");
        title.getStyleClass().add("page-title");

        VBox eventList = new VBox(20);
        for (Event ev : eventController.getEvents()) {
            eventList.getChildren().add(
                    ViewFactory.createAccessCard(ev, getCoordinatorNames(), "👥 Assign Access")
            );
        }

        content.getChildren().addAll(tabs, title, eventList);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    // =========================================================
    // COORDINATOR PAGES
    // =========================================================

    private void showCoordinatorEventsPage() {
        BorderPane layout = createTopShell("EASV Tickets - Event Coordinator Portal", () -> showPortalSelection());

        VBox content = new VBox(25);
        content.setPadding(new Insets(24));

        HBox tabs = ViewFactory.createTabs(
                new TabItem("Events", true, this::showCoordinatorEventsPage),
                new TabItem("Manage Access", false, this::showCoordinatorAccessPage)
        );

        HBox toolbar = new HBox(16);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search events by title or venue...");
        searchField.getStyleClass().add("input-field");
        searchField.setPrefWidth(650);

        Button createBtn = new Button("+  Create Event");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setOnAction(e -> showCreateEventPage());

        toolbar.getChildren().addAll(title, spacer, searchField, createBtn);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 28, 28);
        for (Event ev : eventController.getEvents()) {
            grid.getChildren().add(
                    ViewFactory.createCoordinatorEventCard(ev, () -> showSellTicketPage(ev))
            );
        }

        content.getChildren().addAll(tabs, toolbar, grid);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void showCreateEventPage() {
        BorderPane layout = createTopShell("EASV Tickets - Event Coordinator Portal", () -> showPortalSelection());

        VBox content = new VBox(25);
        content.setPadding(new Insets(20));

        HBox tabs = ViewFactory.createTabs(
                new TabItem("Events", true, this::showCoordinatorEventsPage),
                new TabItem("Manage Access", false, this::showCoordinatorAccessPage)
        );

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Events");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search events by title or venue...");
        searchField.getStyleClass().add("input-field");
        searchField.setPrefWidth(650);

        topRow.getChildren().addAll(title, spacer, searchField);

        VBox form = ViewFactory.createEventForm(
                "Create New Event",
                this::showCoordinatorEventsPage,
                this::showCoordinatorEventsPage
        );

        content.getChildren().addAll(tabs, topRow, form);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void showCoordinatorAccessPage() {
        BorderPane layout = createTopShell("EASV Tickets - Event Coordinator Portal", () -> showPortalSelection());

        VBox content = new VBox(25);
        content.setPadding(new Insets(28));

        HBox tabs = ViewFactory.createTabs(
                new TabItem("Events", false, this::showCoordinatorEventsPage),
                new TabItem("Manage Access", true, this::showCoordinatorAccessPage)
        );

        Label title = new Label("Manage Coordinator Access");
        title.getStyleClass().add("page-title");

        VBox eventList = new VBox(20);
        for (Event ev : eventController.getEvents()) {
            eventList.getChildren().add(
                    ViewFactory.createAccessCard(ev, getCoordinatorNames(), "👥 Assign Access")
            );
        }

        content.getChildren().addAll(tabs, title, eventList);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    // =========================================================
    // SELL TICKET PAGE
    // =========================================================

    private void showSellTicketPage(Event ev) {
        VBox page = new VBox(28);
        page.setPadding(new Insets(40));
        page.getStyleClass().add("main-bg");

        Label pageTitle = new Label("Sell Tickets");
        pageTitle.getStyleClass().add("page-title");

        VBox eventBox = ViewFactory.createSellTicketEventInfo(ev);

        VBox ticketBox = ViewFactory.createTicketSaleForm(
                () -> showPurchaseSuccessPage(ev, "s", "d", "STANDARD", 1, "150.00 DKK")
        );

        page.getChildren().addAll(pageTitle, eventBox, ticketBox);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);

        rootPane.getChildren().setAll(scrollPane);
    }
    private void showPurchaseSuccessPage(Event ev,
                                         String customerName,
                                         String customerEmail,
                                         String ticketType,
                                         int quantity,
                                         String totalPaid) {
        VBox page = new VBox(28);
        page.setPadding(new Insets(20));
        page.getStyleClass().add("main-bg");

        VBox successBox = ViewFactory.createPurchaseSuccessCard(
                ev,
                customerName,
                customerEmail,
                ticketType,
                quantity,
                totalPaid,
                this::showCoordinatorEventsPage
        );

        page.getChildren().add(successBox);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);

        rootPane.getChildren().setAll(scrollPane);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private BorderPane createTopShell(String titleText, Runnable backAction) {
        BorderPane shell = new BorderPane();

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(18, 28, 18, 28));
        topBar.getStyleClass().add("topbar");

        Label title = new Label(titleText);
        title.getStyleClass().add("topbar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("←  Back to Portal Selection");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setOnAction(e -> backAction.run());

        topBar.getChildren().addAll(title, spacer, backBtn);

        shell.setTop(topBar);
        return shell;
    }

    private List<String> getCoordinatorNames() {
        return coordinators.stream().map(c -> c.name).toList();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // =========================================================
    // INNER CLASSES
    // =========================================================

    private static class Coordinator {
        String name;
        String email;
        String phone;
        String role;
        String status;

        Coordinator(String name, String email, String phone, String role, String status) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.status = status;
        }
    }

    public record TabItem(String title, boolean active, Runnable action) { }
}