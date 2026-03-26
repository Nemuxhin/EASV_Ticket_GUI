package Java.Gui;

import Java.Be.Event;
import Java.Be.TicketPurchase;
import Java.Controller.EventController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EASVTicketsApp extends Application {

    private final EventController eventController = new EventController();
    private final StackPane rootPane = new StackPane();

    private final List<Coordinator> coordinators = new ArrayList<>(Arrays.asList(
            new Coordinator("Lars Hansen", "lars.hansen@easv.dk", "+45 12 34 56 78", "Event Coordinator", "Active"),
            new Coordinator("Maria Petersen", "maria.petersen@easv.dk", "+45 23 45 67 89", "Event Coordinator", "Active"),
            new Coordinator("Thomas Nielsen", "thomas.nielsen@easv.dk", "+45 34 56 78 90", "Event Coordinator", "Active")
    ));

    @Override
    public void start(Stage primaryStage) {
        showPortalSelection();

        Scene mainScene = new Scene(rootPane, 1200, 760);
        mainScene.getStylesheets().add(
                getClass().getResource("/CSS/easv-style.css").toExternalForm()
        );

        primaryStage.setTitle("EASV Ticket Management System");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

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

        VBox admin = ViewFactory.createPortalCard("\uD83D\uDC64", "Admin", "Manage Coordinators");
        admin.setOnMouseClicked(e -> showLogin("Admin"));

        VBox coordinator = ViewFactory.createPortalCard("\uD83D\uDCC5", "Event Coordinator", "Manage Events");
        coordinator.setOnMouseClicked(e -> showLogin("Coordinator"));

        cards.getChildren().addAll(admin, coordinator);
        layout.getChildren().addAll(header, cards);

        rootPane.getChildren().setAll(layout);
    }

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

        Button backBtn = new Button("\uD83C\uDFE0 Back to Portal Selection");
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

    private void showAdminCoordinatorsPage() {
        BorderPane layout = createTopShell("EASV Tickets - Admin Portal", this::showPortalSelection);

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
                            () -> handleDeleteCoordinator(c)
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
        BorderPane layout = createTopShell("EASV Tickets - Admin Portal", this::showPortalSelection);

        VBox content = new VBox(25);
        content.setPadding(new Insets(28));

        HBox tabs = ViewFactory.createTabs(
                new TabItem("Coordinators", true, this::showAdminCoordinatorsPage),
                new TabItem("Manage Access", false, this::showAdminAccessPage)
        );

        Label title = new Label("Event Coordinators");
        title.getStyleClass().add("page-title");

        ViewFactory.CoordinatorForm[] coordinatorFormRef = new ViewFactory.CoordinatorForm[1];
        ViewFactory.CoordinatorForm form = ViewFactory.createCoordinatorForm(
                "Create New Coordinator",
                () -> handleCreateCoordinator(coordinatorFormRef[0]),
                this::showAdminCoordinatorsPage
        );
        coordinatorFormRef[0] = form;

        content.getChildren().addAll(tabs, title, form.getRoot());

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void showAdminAccessPage() {
        BorderPane layout = createTopShell("EASV Tickets - Admin Portal", this::showPortalSelection);

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
                    ViewFactory.createAccessCard(ev, getCoordinatorNames())
            );
        }

        content.getChildren().addAll(tabs, title, eventList);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void showCoordinatorEventsPage() {
        BorderPane layout = createTopShell("EASV Tickets - Event Coordinator Portal", this::showPortalSelection);

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
                    ViewFactory.createCoordinatorEventCard(
                            ev,
                            () -> showSellTicketPage(ev),
                            () -> handleDeleteEvent(ev)
                    )
            );
        }

        content.getChildren().addAll(tabs, toolbar, grid);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void showCreateEventPage() {
        BorderPane layout = createTopShell("EASV Tickets - Event Coordinator Portal", this::showPortalSelection);

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

        ViewFactory.EventForm[] eventFormRef = new ViewFactory.EventForm[1];
        ViewFactory.EventForm eventForm = ViewFactory.createEventForm(
                "Create New Event",
                () -> handleCreateEvent(eventFormRef[0]),
                this::showCoordinatorEventsPage,
                this::showCoordinatorEventsPage
        );

        eventFormRef[0] = eventForm;

        content.getChildren().addAll(tabs, topRow, eventForm.getRoot());

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void showCoordinatorAccessPage() {
        BorderPane layout = createTopShell("EASV Tickets - Event Coordinator Portal", this::showPortalSelection);

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
                    ViewFactory.createAccessCard(ev, getCoordinatorNames())
            );
        }

        content.getChildren().addAll(tabs, title, eventList);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        layout.setCenter(scrollPane);
        rootPane.getChildren().setAll(layout);
    }

    private void handleCreateEvent(ViewFactory.EventForm eventForm) {
        String title = eventForm.getTitleField().getText();
        String date = eventForm.getDateField().getText();
        String time = eventForm.getTimeField().getText();
        String venue = eventForm.getVenueField().getText();
        String capacity = eventForm.getCapacityField().getText();
        String price = eventForm.getPriceField().getText();
        String notes = eventForm.getNotesArea().getText();

        String validationError = eventController.validateEvent(
                title,
                date,
                time,
                venue,
                capacity,
                price
        );

        if (validationError != null) {
            AlertHelper.showError("Invalid Event", validationError);
            return;
        }

        eventController.createEvent(title, date, time, venue, notes, price);
        AlertHelper.showInfo("Event Created", "The event was created successfully.");
        showCoordinatorEventsPage();
    }

    private void handleDeleteEvent(Event event) {
        eventController.deleteEvent(event);
        AlertHelper.showInfo("Event Deleted", "The event was deleted successfully.");
        showCoordinatorEventsPage();
    }

    private void handleDeleteCoordinator(Coordinator coordinator) {
        coordinators.remove(coordinator);
        AlertHelper.showInfo("Coordinator Deleted", "The coordinator was deleted successfully.");
        showAdminCoordinatorsPage();
    }

    private void handleCreateCoordinator(ViewFactory.CoordinatorForm coordinatorForm) {
        String name = coordinatorForm.getNameField().getText();
        String email = coordinatorForm.getEmailField().getText();
        String phone = coordinatorForm.getPhoneField().getText();
        String role = coordinatorForm.getRoleBox().getValue();

        String validationError = validateCoordinator(name, email, phone, role);
        if (validationError != null) {
            AlertHelper.showError("Invalid Coordinator", validationError);
            return;
        }

        coordinators.add(new Coordinator(
                name.trim(),
                email.trim(),
                phone.trim(),
                role,
                "Active"
        ));

        AlertHelper.showInfo("Coordinator Created", "The coordinator was created successfully.");
        showAdminCoordinatorsPage();
    }

    private String validateCoordinator(String name, String email, String phone, String role) {
        if (name == null || name.trim().isEmpty()) {
            return "Please enter the coordinator's full name.";
        }

        String trimmedEmail = email == null ? "" : email.trim();
        String trimmedPhone = phone == null ? "" : phone.trim();

        if (email == null || email.trim().isEmpty()) {
            return "Please enter the coordinator's email address.";
        }

        if (!eventController.isValidEmail(trimmedEmail)) {
            return "Please enter a valid email address.";
        }

        if (trimmedPhone.isEmpty()) {
            return "Please enter the coordinator's phone number.";
        }

        if (!trimmedPhone.matches("^\\+?[0-9][0-9\\s-]{6,}$")) {
            return "Please enter a valid phone number.";
        }

        if (role == null || role.trim().isEmpty()) {
            return "Please select a role.";
        }

        return null;
    }

    private void showSellTicketPage(Event ev) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(20, 24, 20, 24));
        page.getStyleClass().add("main-bg");

        Button backToEventsBtn = new Button("Back to Events");
        backToEventsBtn.getStyleClass().add("primary-btn");
        backToEventsBtn.setOnAction(e -> showCoordinatorEventsPage());

        Label pageTitle = new Label("Sell Tickets");
        pageTitle.getStyleClass().add("page-title");

        VBox eventBox = ViewFactory.createSellTicketEventInfo(ev);

        ViewFactory.TicketSaleForm[] ticketFormRef = new ViewFactory.TicketSaleForm[1];
        ViewFactory.TicketSaleForm ticketForm = ViewFactory.createTicketSaleForm(
                ev,
                () -> handlePurchaseConfirmation(ev, ticketFormRef[0])
        );
        ticketFormRef[0] = ticketForm;
        ticketForm.getTotalLabel().setText(formatPrice(
                eventController.calculateTotalPrice(ev, ticketForm.getSelectedTicketType(), ticketForm.getQuantity())
        ));

        ticketForm.selectedTicketTypeProperty().addListener((obs, oldValue, newValue) ->
                updateTotalPrice(ev, ticketForm));
        ticketForm.quantityProperty().addListener((obs, oldValue, newValue) ->
                updateTotalPrice(ev, ticketForm));

        page.getChildren().addAll(backToEventsBtn, pageTitle, eventBox, ticketForm.getRoot());

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
        VBox page = new VBox(16);
        page.setPadding(new Insets(16));
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

    private void handlePurchaseConfirmation(Event ev, ViewFactory.TicketSaleForm ticketForm) {
        String customerName = ticketForm.getNameField().getText();
        String customerEmail = ticketForm.getEmailField().getText();
        String ticketType = ticketForm.getSelectedTicketType();
        int quantity = ticketForm.getQuantity();

        String validationError = eventController.validatePurchase(
                customerName,
                customerEmail,
                ticketType,
                quantity
        );

        if (validationError != null) {
            AlertHelper.showError("Invalid Purchase", validationError);
            return;
        }

        TicketPurchase purchase = eventController.createTicketPurchase(
                ev,
                customerName,
                customerEmail,
                ticketType,
                quantity
        );

        showPurchaseSuccessPage(
                purchase.getEvent(),
                purchase.getCustomerName(),
                purchase.getCustomerEmail(),
                purchase.getTicketType(),
                purchase.getQuantity(),
                formatPrice(purchase.getTotalPrice())
        );
    }

    private void updateTotalPrice(Event ev, ViewFactory.TicketSaleForm ticketForm) {
        double totalPrice = eventController.calculateTotalPrice(
                ev,
                ticketForm.getSelectedTicketType(),
                ticketForm.getQuantity()
        );
        ticketForm.getTotalLabel().setText(formatPrice(totalPrice));
    }

    private String formatPrice(double totalPrice) {
        return String.format(Locale.US, "%.2f DKK", totalPrice);
    }

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

        Button backBtn = new Button("\u2190  Back to Portal Selection");
        backBtn.getStyleClass().add("primary-btn");
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
