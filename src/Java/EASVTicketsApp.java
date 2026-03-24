package Java;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EASVTicketsApp extends Application {

    private Stage window;
    private Scene mainScene;
    private StackPane rootPane;

    // Shared event list: every event screen reads from this same source
    private List<Event> eventsList;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("EASV Ticket Management System");

        // Sample data used to render the first version of the UI
        eventsList = new ArrayList<>();
        eventsList.add(new Event(
                "EASV Graduation Ceremony 2026",
                "20 Jun 2026 at 14:00",
                "20 Jun 2026 at 16:00",
                "EASV Campus, Esbjerg",
                "Use the main hall entrance near the parking area.",
                "Annual graduation ceremony for EASV students",
                "Free",
                "Available",
                new String[]{"Event Coordinator 1", "Event Coordinator 2"}
        ));
        eventsList.add(new Event(
                "Tech Innovation Summit",
                "15 Jul 2026 at 09:00",
                "",
                "Innovation Hub, Esbjerg",
                "",
                "Annual technology and innovation conference",
                "150 DKK",
                "Selling Fast",
                new String[]{"Event Coordinator 3"}
        ));
        eventsList.add(new Event(
                "Danish Business Networking",
                "22 Aug 2026 at 18:00",
                "22 Aug 2026 at 21:00",
                "Copenhagen Convention Center",
                "Meet at the north lobby reception desk.",
                "Business networking event for professionals",
                "500 DKK",
                "Available",
                new String[]{"Event Coordinator 4", "Event Coordinator 5"}
        ));

        rootPane = new StackPane();
        mainScene = new Scene(rootPane, 1200, 800);
        mainScene.getStylesheets().add(getClass().getResource("/css/easv-style.css").toExternalForm());

        // The app starts from the portal selection screen
        showPortalSelection();

        window.setScene(mainScene);
        window.show();
    }

    // ==========================================
    // PORTAL & LOGIN
    // ==========================================
    // First screen: user chooses which portal to enter
    private void showPortalSelection() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER); layout.getStyleClass().add("main-bg");
        VBox header = createHeader("EASV Tickets", "Event Ticket Management System", "Select Portal", "Choose your access level");

        HBox cardsBox = new HBox(20); cardsBox.setAlignment(Pos.CENTER);
        VBox adminCard = createPortalCard("👤", "Admin", "Manage coordinators");
        adminCard.setOnMouseClicked(e -> showLogin("Admin"));
        VBox coordCard = createPortalCard("📅", "Event Coordinator", "Manage events");
        coordCard.setOnMouseClicked(e -> showLogin("Event Coordinator"));
        VBox customerCard = createPortalCard("🛍️", "Customer", "Buy tickets");
        customerCard.setOnMouseClicked(e -> showCustomerDashboard());

        cardsBox.getChildren().addAll(adminCard, coordCard, customerCard);
        layout.getChildren().addAll(header, cardsBox);
        rootPane.getChildren().setAll(layout);
    }

    // Login is only visual here: it routes the user to the selected dashboard
    private void showLogin(String role) {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER); layout.getStyleClass().add("main-bg");
        VBox header = createHeader("EASV Tickets - " + role, "", "", "");

        VBox formBox = new VBox(15);
        formBox.setMaxWidth(350); formBox.getStyleClass().add("login-form"); formBox.setPadding(new Insets(30));

        Label userLbl = new Label("Username *"); userLbl.getStyleClass().add("form-label");
        TextField userField = new TextField(); userField.getStyleClass().add("input-field");
        Label passLbl = new Label("Password *"); passLbl.getStyleClass().add("form-label");
        PasswordField passField = new PasswordField(); passField.getStyleClass().add("input-field");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("primary-btn"); loginBtn.setMaxWidth(Double.MAX_VALUE);

        // Route login based on role
        loginBtn.setOnAction(e -> {
            if (role.equals("Admin")) showAdminDashboard("Coordinators");
            else showCoordinatorDashboard("Events");
        });

        Button backBtn = new Button("🏠 Back to Portal Selection");
        backBtn.getStyleClass().add("secondary-btn"); backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> showPortalSelection());

        formBox.getChildren().addAll(userLbl, userField, passLbl, passField, loginBtn, new Separator(), backBtn);
        layout.getChildren().addAll(header, formBox);
        rootPane.getChildren().setAll(layout);
    }

    // ==========================================
    // DASHBOARDS (WITH SIDEBAR)
    // ==========================================
    // Admin dashboard: left menu stays fixed, center changes by active tab
    private void showAdminDashboard(String activeTab) {
        BorderPane layout = new BorderPane(); layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar("Admin", activeTab));

        VBox content;
        if (activeTab.equals("Events")) {
            content = createDashboardContent("Manage Events", "ADMIN_EVENTS");
        } else {
            content = createCoordinatorsContent();
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true); scroll.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        layout.setCenter(scroll);
        rootPane.getChildren().setAll(layout);
    }

    // Coordinator dashboard uses the same idea, but with coordinator views
    private void showCoordinatorDashboard(String activeTab) {
        BorderPane layout = new BorderPane(); layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar("Event Coordinator", activeTab));

        VBox content = createDashboardContent(activeTab.equals("Events") ? "Events" : "Manage Access",
                activeTab.equals("Events") ? "COORD_EVENTS" : "COORD_ACCESS");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true); scroll.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        layout.setCenter(scroll);
        rootPane.getChildren().setAll(layout);
    }

    // Sidebar only builds the menu and connects buttons to screens
    private VBox createSidebar(String role, String activeItem) {
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220); sidebar.setPadding(new Insets(20));

        VBox logoBox = new VBox();
        Label logo1 = new Label("SEA\nErhvervsakademi"); logo1.getStyleClass().add("sidebar-logo");
        Label logo2 = new Label("EASV Tickets"); logo2.getStyleClass().add("sidebar-sub");
        logoBox.getChildren().addAll(logo1, logo2);

        VBox roleBox = new VBox(5); roleBox.getStyleClass().add("sidebar-role-box");
        Label l1 = new Label("Logged in as"); l1.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 11px;");
        Label l2 = new Label(role); l2.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        roleBox.getChildren().addAll(l1, l2);

        VBox menuBox = new VBox(10);
        if (role.equals("Admin")) {
            menuBox.getChildren().add(createMenuBtn("👥 Coordinators", activeItem.equals("Coordinators"), e -> showAdminDashboard("Coordinators")));
            menuBox.getChildren().add(createMenuBtn("📅 Events", activeItem.equals("Events"), e -> showAdminDashboard("Events")));
        } else {
            menuBox.getChildren().add(createMenuBtn("📅 Events", activeItem.equals("Events"), e -> showCoordinatorDashboard("Events")));
            menuBox.getChildren().add(createMenuBtn("👥 Manage Access", activeItem.equals("Manage Access"), e -> showCoordinatorDashboard("Manage Access")));
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Button logoutBtn = new Button("🚪 Logout"); logoutBtn.getStyleClass().add("sidebar-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> showPortalSelection());

        sidebar.getChildren().addAll(logoBox, roleBox, menuBox, spacer, logoutBtn);
        return sidebar;
    }

    private Button createMenuBtn(String text, boolean isActive, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        btn.getStyleClass().add(isActive ? "sidebar-menu-btn-active" : "sidebar-menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(action);
        return btn;
    }

    // ==========================================
    // ADMIN: COORDINATORS CONTENT
    // ==========================================
    // This section is separate because it does not depend on eventsList
    private VBox createCoordinatorsContent() {
        VBox content = new VBox(20); content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Manage Coordinators"); title.getStyleClass().add("page-title");

        TextField searchBar = new TextField(); searchBar.setPromptText("🔍 Search coordinators...");
        searchBar.getStyleClass().add("search-bar"); searchBar.setMaxWidth(400);

        Button createBtn = new Button("＋ Create Coordinator");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setPrefWidth(1000);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000); // Forces it to try to lay out horizontally up to 1000px before wrapping

        String[][] dummyCoordinators = {
                {"Sarah Jensen", "s.jensen@easv.dk"},
                {"Mikkel Andersen", "m.andersen@easv.dk"},
                {"Laura Nielsen", "l.nielsen@easv.dk"},
                {"Peter Christiansen", "p.chris@easv.dk"}
        };

        for (String[] c : dummyCoordinators) {
            VBox card = new VBox(10); card.getStyleClass().add("event-card");
            Label nLbl = new Label("👤 " + c[0]); nLbl.getStyleClass().add("card-title");
            Label eLbl = new Label("✉ " + c[1]); eLbl.getStyleClass().add("card-text");
            Button delBtn = new Button("🗑 Delete"); delBtn.getStyleClass().add("danger-btn"); delBtn.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().addAll(nLbl, eLbl, new Separator(), delBtn);
            grid.getChildren().add(card);
        }

        content.getChildren().addAll(title, searchBar, createBtn, grid);
        return content;
    }

    // ==========================================
    // CUSTOMER DASHBOARD & BUY TICKET
    // ==========================================
    private void showCustomerDashboard() {
        BorderPane layout = new BorderPane(); layout.getStyleClass().add("main-bg");
        layout.setTop(createCustomerTopBar("🏠 Back to Portal Selection", e -> showPortalSelection()));

        VBox content = createDashboardContent("Events", "CUSTOMER");
        Label subTitle = new Label("Browse and purchase event tickets");
        subTitle.getStyleClass().add("page-subtitle");
        content.getChildren().add(1, subTitle);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true); scroll.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        layout.setCenter(scroll);
        rootPane.getChildren().setAll(layout);
    }

    private void showBuyTicketScreen(Event ev) {
        BorderPane layout = new BorderPane(); layout.getStyleClass().add("main-bg");
        layout.setTop(createCustomerTopBar("🏠 Back to Portal Selection", e -> showPortalSelection()));

        VBox content = new VBox(20);
        content.setMaxWidth(650);

        Label pageTitle = new Label("Buy Ticket"); pageTitle.getStyleClass().add("page-title");

        VBox eventCard = new VBox(10); eventCard.getStyleClass().add("buy-ticket-card");
        HBox top = new HBox();
        Label tLbl = new Label(ev.getTitle()); tLbl.getStyleClass().add("card-title-large");
        Label sLbl = new Label(ev.getStatus()); sLbl.getStyleClass().add(getStatusStyle(ev.getStatus()));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(tLbl, spacer, sLbl);
        Label startLbl = new Label("🕒 Starts: " + ev.getStartDateTime()); startLbl.getStyleClass().add("card-text");
        Label locationLbl = new Label("📍 " + ev.getLocation()); locationLbl.getStyleClass().add("card-text");
        Label notesLbl = new Label(ev.getNotes()); notesLbl.getStyleClass().add("card-text"); notesLbl.setPadding(new Insets(10,0,0,0));
        eventCard.getChildren().addAll(top, startLbl);
        addOptionalEventInfo(eventCard, ev);
        eventCard.getChildren().addAll(locationLbl, new Separator(), notesLbl);

        VBox selectionCard = new VBox(20); selectionCard.getStyleClass().add("buy-ticket-card");
        Label typeTitle = new Label("Select Ticket Type"); typeTitle.getStyleClass().add("card-title");

        HBox typesBox = new HBox(15);
        VBox type1 = createTicketTypeBox("Standard", ev.getPrice(), true);
        VBox type2 = createTicketTypeBox("VIP", "225 DKK", false);
        VBox type3 = createTicketTypeBox("Student", "105 DKK", false);
        typesBox.getChildren().addAll(type1, type2, type3);

        Label qtyTitle = new Label("Quantity"); qtyTitle.getStyleClass().add("form-label");
        HBox qtyBox = new HBox(15); qtyBox.setAlignment(Pos.CENTER_LEFT);
        Button minusBtn = new Button("-"); minusBtn.getStyleClass().add("qty-btn");
        Label qtyLbl = new Label("1"); qtyLbl.setStyle("-fx-font-size: 16px;");
        Button plusBtn = new Button("+"); plusBtn.getStyleClass().add("qty-btn");
        qtyBox.getChildren().addAll(minusBtn, qtyLbl, plusBtn);

        HBox totalBox = new HBox();
        Label totLbl = new Label("Total Price"); totLbl.getStyleClass().add("card-text");
        Label priceLbl = new Label(ev.getPrice()); priceLbl.getStyleClass().add("price-text-large");
        Region tSpacer = new Region(); HBox.setHgrow(tSpacer, Priority.ALWAYS);
        totalBox.getChildren().addAll(totLbl, tSpacer, priceLbl);

        Button confirmBtn = new Button("Confirm Purchase");
        confirmBtn.getStyleClass().add("primary-btn"); confirmBtn.setMaxWidth(Double.MAX_VALUE);

        selectionCard.getChildren().addAll(typeTitle, typesBox, qtyTitle, qtyBox, new Separator(), totalBox, confirmBtn);

        final int[] qty = {1};
        plusBtn.setOnAction(e -> { qty[0]++; qtyLbl.setText(String.valueOf(qty[0])); });
        minusBtn.setOnAction(e -> { if (qty[0] > 1) { qty[0]--; qtyLbl.setText(String.valueOf(qty[0])); } });

        content.getChildren().addAll(pageTitle, eventCard, selectionCard);

        VBox centeringContainer = new VBox(content);
        centeringContainer.setAlignment(Pos.TOP_CENTER);
        centeringContainer.setPadding(new Insets(40, 20, 40, 20));

        ScrollPane scroll = new ScrollPane(centeringContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");

        layout.setCenter(scroll);
        rootPane.getChildren().setAll(layout);
    }

    private VBox createTicketTypeBox(String type, String price, boolean isActive) {
        VBox box = new VBox(5); box.setAlignment(Pos.CENTER);
        box.getStyleClass().add(isActive ? "ticket-type-box-active" : "ticket-type-box");
        Label t = new Label(type); t.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A202C;");
        Label p = new Label(price); p.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 12px;");
        box.getChildren().addAll(t, p);
        return box;
    }

    private HBox createCustomerTopBar(String btnText, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        HBox topBar = new HBox(); topBar.getStyleClass().add("dark-top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT); topBar.setPadding(new Insets(10, 20, 10, 20));
        VBox logoBox = new VBox();
        Label logo1 = new Label("SEA\nErhvervsakademi"); logo1.getStyleClass().add("top-bar-logo");
        Label logo2 = new Label("EASV Tickets - Customer Portal"); logo2.getStyleClass().add("top-bar-sub");
        logoBox.getChildren().addAll(logo1, logo2);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btn = new Button(btnText); btn.getStyleClass().add("secondary-btn-dark");
        btn.setOnAction(action);
        topBar.getChildren().addAll(logoBox, spacer, btn);
        return topBar;
    }

    // ==========================================
    // SHARED DASHBOARD CONTENT BUILDER
    // ==========================================
    private VBox createDashboardContent(String titleText, String viewMode) {
        VBox content = new VBox(20); content.setPadding(new Insets(30, 50, 30, 50));

        HBox headerBox = new HBox(); headerBox.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(titleText); title.getStyleClass().add("page-title");
        headerBox.getChildren().add(title);

        TextField searchBar = new TextField(); searchBar.setPromptText("🔍 Search events...");
        searchBar.getStyleClass().add("search-bar"); searchBar.setMaxWidth(400);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        if (viewMode.equals("COORD_EVENTS")) {
            content.getChildren().add(createCreateEventButton(viewMode));
        }

        for (Event ev : eventsList) {
            grid.getChildren().add(buildDynamicCard(ev, viewMode));
        }

        content.getChildren().addAll(headerBox, searchBar, grid);
        return content;
    }

    // ==========================================
    // SAMU TASKS: EVENT CREATE / SHOW / DELETE
    // ==========================================
    private Button createCreateEventButton(String viewMode) {
        Button createBtn = new Button("＋ Create Event");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setPrefWidth(1000);
        createBtn.setOnAction(e -> showCreateEventDialog(viewMode));
        return createBtn;
    }

    // (Samu) Create event dialog with required and optional fields
    private void showCreateEventDialog(String viewMode) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.initOwner(window);
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
        notesField.setPrefRowCount(3);
        notesField.setWrapText(true);
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
            eventsList.add(result.get());
            refreshView(viewMode);
        }
    }

    // (Samu) Check only the required fields
    private String validateRequiredInput(String title, String startDateTime, String location,
                                         String notes, String price) {
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

    // (Samu) Convert the coordinators text into a simple array
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

    // (Samu) Refresh the correct screen after add or delete
    private void refreshView(String viewMode) {
        if (viewMode.equals("ADMIN_EVENTS")) {
            showAdminDashboard("Events");
        } else if (viewMode.equals("COORD_EVENTS")) {
            showCoordinatorDashboard("Events");
        } else if (viewMode.equals("COORD_ACCESS")) {
            showCoordinatorDashboard("Manage Access");
        } else if (viewMode.equals("CUSTOMER")) {
            showCustomerDashboard();
        }
    }

    // (Samu) Ask for confirmation before deleting
    private void confirmDeleteEvent(Event ev, String viewMode) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(window);
        confirmation.setTitle("Delete Event");
        confirmation.setHeaderText("Delete \"" + ev.getTitle() + "\"?");
        confirmation.setContentText("This event will be removed from every event overview.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteEvent(ev, viewMode);
        }
    }

    // (Samu) Delete from the shared list and show an error if it fails
    private void deleteEvent(Event ev, String viewMode) {
        boolean eventDeleted = eventsList.remove(ev);

        if (eventDeleted) {
            refreshView(viewMode);
        } else {
            showErrorMessage("Delete failed", "The selected event could not be deleted.");
        }
    }

    // (Samu) Reuse the same delete button in admin event cards
    private Button createDeleteEventButton(Event ev, String viewMode) {
        Button delBtn = new Button("🗑 Delete Event");
        delBtn.getStyleClass().add("danger-btn");
        delBtn.setMaxWidth(Double.MAX_VALUE);
        delBtn.setOnAction(e -> confirmDeleteEvent(ev, viewMode));
        return delBtn;
    }

    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(window);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addOptionalEventInfo(VBox container, Event ev) {
        if (ev.hasEndDateTime()) {
            Label endLbl = new Label("🕓 Ends: " + ev.getEndDateTime());
            endLbl.getStyleClass().add("card-text");
            container.getChildren().add(endLbl);
        }

        if (ev.hasLocationGuidance()) {
            Label guidanceLbl = new Label("🧭 Guidance: " + ev.getLocationGuidance());
            guidanceLbl.getStyleClass().add("card-text");
            container.getChildren().add(guidanceLbl);
        }
    }

    private String getStatusStyle(String status) {
        if (status.equals("Available")) {
            return "status-avail";
        }
        return "status-fast";
    }

    private VBox buildDynamicCard(Event ev, String viewMode) {
        VBox card = new VBox(10); card.getStyleClass().add("event-card");

        HBox top = new HBox();
        Label tLbl = new Label(ev.getTitle()); tLbl.getStyleClass().add("card-title");
        Label sLbl = new Label(ev.getStatus()); sLbl.getStyleClass().add(getStatusStyle(ev.getStatus()));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(tLbl, spacer, sLbl);

        Label startLbl = new Label("🕒 Starts: " + ev.getStartDateTime()); startLbl.getStyleClass().add("card-text");
        Label locationLbl = new Label("📍 " + ev.getLocation()); locationLbl.getStyleClass().add("card-text");
        Label nHead = new Label("Notes"); nHead.getStyleClass().add("notes-head");
        Label nLbl = new Label(ev.getNotes()); nLbl.getStyleClass().add("card-text");

        card.getChildren().addAll(top, startLbl);
        addOptionalEventInfo(card, ev);
        card.getChildren().addAll(locationLbl, nHead, nLbl, new Separator());

        if (viewMode.equals("CUSTOMER")) {
            Label pLbl = new Label(ev.getPrice()); pLbl.getStyleClass().add("price-text");
            Button buyBtn = new Button("Buy Ticket"); buyBtn.getStyleClass().add("primary-btn"); buyBtn.setMaxWidth(Double.MAX_VALUE);
            buyBtn.setOnAction(e -> showBuyTicketScreen(ev));
            card.getChildren().addAll(pLbl, buyBtn);
        } else if (viewMode.equals("ADMIN_EVENTS")) {
            Label pLbl = new Label(ev.getPrice()); pLbl.getStyleClass().add("price-text");
            card.getChildren().addAll(pLbl, createDeleteEventButton(ev, viewMode));
        } else if (viewMode.equals("COORD_EVENTS")) {
            Label pLbl = new Label(ev.getPrice()); pLbl.getStyleClass().add("price-text");
            card.getChildren().add(pLbl);
        } else if (viewMode.equals("COORD_ACCESS")) {
            Label asgnHead = new Label("Assigned Coordinators");
            asgnHead.getStyleClass().add("notes-head");

            FlowPane pillBox = new FlowPane(5, 5);
            for (String c : ev.getCoordinators()) {
                Label pill = new Label(c);
                pill.getStyleClass().add("coord-pill");
                pillBox.getChildren().add(pill);
            }

            Button assignBtn = new Button("👥 Assign Access");
            assignBtn.getStyleClass().add("primary-btn");
            assignBtn.setMaxWidth(Double.MAX_VALUE);

            card.getChildren().addAll(asgnHead, pillBox, assignBtn);
        }

        return card;
    }

    private VBox createHeader(String aT, String aS, String pT, String pS) {
        VBox box = new VBox(5); box.setAlignment(Pos.CENTER);
        Label l1 = new Label("SEA"); l1.getStyleClass().add("logo-large");
        Label l2 = new Label("Erhvervsakademi"); l2.getStyleClass().add("logo-small");
        Label l3 = new Label(aT); l3.getStyleClass().add("app-title");
        box.getChildren().addAll(l1, l2, l3);
        if (!aS.isEmpty()) { Label l4 = new Label(aS); l4.getStyleClass().add("app-subtitle"); box.getChildren().add(l4); }
        if (!pT.isEmpty()) {
            VBox spacing = new VBox(); spacing.setMinHeight(20);
            Label pt = new Label(pT); pt.getStyleClass().add("page-title-center");
            Label ps = new Label(pS); ps.getStyleClass().add("page-subtitle-center");
            box.getChildren().addAll(spacing, pt, ps);
        }
        return box;
    }

    private VBox createPortalCard(String i, String t, String s) {
        VBox c = new VBox(10); c.setAlignment(Pos.CENTER); c.getStyleClass().add("portal-card");
        Label ic = new Label(i); ic.getStyleClass().add("portal-icon");
        Label tL = new Label(t); tL.getStyleClass().add("card-title");
        Label sL = new Label(s); sL.getStyleClass().add("card-subtitle");
        c.getChildren().addAll(ic, tL, sL); return c;
    }

    public static void main(String[] args) { launch(args); }
}

