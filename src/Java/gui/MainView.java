package Java.gui;

import Java.be.Event;
import Java.be.Ticket;
import Java.be.User;
import Java.controller.EventController;
import Java.controller.LoginController;
import Java.controller.TicketController;
import Java.controller.UserController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class MainView {

    private final Stage window;
    private final StackPane rootPane;

    private final LoginController loginController;
    private final EventController eventController;
    private final UserController userController;
    private final TicketController ticketController;

    public MainView(Stage window) {
        this.window = window;
        this.rootPane = new StackPane();

        this.loginController = new LoginController();
        this.eventController = new EventController();
        this.userController = new UserController();
        this.ticketController = new TicketController();

        showPortalSelection();
    }

    public Parent getRoot() {
        return rootPane;
    }

    // ==========================================
    // PORTAL & LOGIN
    // ==========================================
    private void showPortalSelection() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-bg");

        VBox header = createHeader(
                "EASV Tickets",
                "Event Ticket Management System",
                "Select Portal",
                "Choose your access level"
        );

        HBox cardsBox = new HBox(20);
        cardsBox.setAlignment(Pos.CENTER);

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

    private void showLogin(String role) {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-bg");

        VBox header = createHeader("EASV Tickets - " + role, "", "", "");

        VBox formBox = new VBox(15);
        formBox.setMaxWidth(350);
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
            boolean success = loginController.login(
                    userField.getText(),
                    passField.getText(),
                    role
            );

            if (!success) {
                showError("Login failed", "Please enter both username and password.");
                return;
            }

            if ("Admin".equals(role)) {
                showAdminDashboard("Coordinators");
            } else {
                showCoordinatorDashboard("Events");
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

    // ==========================================
    // DASHBOARDS (WITH SIDEBAR)
    // ==========================================
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

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        layout.setCenter(scroll);

        rootPane.getChildren().setAll(layout);
    }

    private void showCoordinatorDashboard(String activeTab) {
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setLeft(createSidebar("Event Coordinator", activeTab));

        VBox content = createDashboardContent(
                "Events".equals(activeTab) ? "Events" : "Manage Access",
                "Events".equals(activeTab) ? "COORD_EVENTS" : "COORD_ACCESS"
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        layout.setCenter(scroll);

        rootPane.getChildren().setAll(layout);
    }

    private VBox createSidebar(String role, String activeItem) {
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20));

        VBox logoBox = new VBox();
        Label logo1 = new Label("SEA\nErhvervsakademi");
        logo1.getStyleClass().add("sidebar-logo");

        Label logo2 = new Label("EASV Tickets");
        logo2.getStyleClass().add("sidebar-sub");

        logoBox.getChildren().addAll(logo1, logo2);

        VBox roleBox = new VBox(5);
        roleBox.getStyleClass().add("sidebar-role-box");

        Label l1 = new Label("Logged in as");
        l1.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 11px;");

        Label l2 = new Label(role);
        l2.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        roleBox.getChildren().addAll(l1, l2);

        VBox menuBox = new VBox(10);

        if ("Admin".equals(role)) {
            menuBox.getChildren().add(
                    createMenuBtn("👥 Coordinators", "Coordinators".equals(activeItem),
                            e -> showAdminDashboard("Coordinators"))
            );
            menuBox.getChildren().add(
                    createMenuBtn("📅 Events", "Events".equals(activeItem),
                            e -> showAdminDashboard("Events"))
            );
        } else {
            menuBox.getChildren().add(
                    createMenuBtn("📅 Events", "Events".equals(activeItem),
                            e -> showCoordinatorDashboard("Events"))
            );
            menuBox.getChildren().add(
                    createMenuBtn("👥 Manage Access", "Manage Access".equals(activeItem),
                            e -> showCoordinatorDashboard("Manage Access"))
            );
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().add("sidebar-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> showPortalSelection());

        sidebar.getChildren().addAll(logoBox, roleBox, menuBox, spacer, logoutBtn);
        return sidebar;
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

    // ==========================================
    // ADMIN: COORDINATORS CONTENT
    // ==========================================
    private VBox createCoordinatorsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        Label title = new Label("Manage Coordinators");
        title.getStyleClass().add("page-title");

        TextField searchBar = new TextField();
        searchBar.setPromptText("🔍 Search coordinators...");
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
            showAdminDashboard("Coordinators");
        });

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        List<User> coordinators = userController.getUsersByRole("Event Coordinator");

        for (User user : coordinators) {
            VBox card = new VBox(10);
            card.getStyleClass().add("event-card");

            Label nLbl = new Label("👤 " + user.getName());
            nLbl.getStyleClass().add("card-title");

            Label eLbl = new Label("✉ " + user.getEmail());
            eLbl.getStyleClass().add("card-text");

            Button delBtn = new Button("🗑 Delete");
            delBtn.getStyleClass().add("danger-btn");
            delBtn.setMaxWidth(Double.MAX_VALUE);
            delBtn.setOnAction(e -> {
                userController.deleteUser(user);
                showAdminDashboard("Coordinators");
            });

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
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setTop(createCustomerTopBar("🏠 Back to Portal Selection", e -> showPortalSelection()));

        VBox content = createDashboardContent("Events", "CUSTOMER");

        Label subTitle = new Label("Browse and purchase event tickets");
        subTitle.getStyleClass().add("page-subtitle");
        content.getChildren().add(1, subTitle);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        layout.setCenter(scroll);

        rootPane.getChildren().setAll(layout);
    }

    private void showBuyTicketScreen(Event ev) {
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-bg");
        layout.setTop(createCustomerTopBar("🏠 Back to Events", e -> showCustomerDashboard()));

        VBox content = new VBox(20);
        content.setMaxWidth(700);

        Label pageTitle = new Label("Buy Ticket");
        pageTitle.getStyleClass().add("page-title");

        VBox eventCard = new VBox(10);
        eventCard.getStyleClass().add("buy-ticket-card");

        HBox top = new HBox();
        Label tLbl = new Label(ev.getTitle());
        tLbl.getStyleClass().add("card-title-large");

        Label sLbl = new Label(ev.getStatus());
        sLbl.getStyleClass().add(
                "Available".equals(ev.getStatus()) ? "status-avail" : "status-fast"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        top.getChildren().addAll(tLbl, spacer, sLbl);

        Label dLbl = new Label("🕒 " + ev.getDate());
        dLbl.getStyleClass().add("card-text");

        Label lLbl = new Label("📍 " + ev.getLocation());
        lLbl.getStyleClass().add("card-text");

        Label nLbl = new Label(ev.getNotes());
        nLbl.getStyleClass().add("card-text");
        nLbl.setPadding(new Insets(10, 0, 0, 0));

        eventCard.getChildren().addAll(top, dLbl, lLbl, new Separator(), nLbl);

        VBox selectionCard = new VBox(20);
        selectionCard.getStyleClass().add("buy-ticket-card");

        Label typeTitle = new Label("Select Ticket Type");
        typeTitle.getStyleClass().add("card-title");

        FlowPane typesBox = new FlowPane(15, 15);

        List<Event.TicketOption> options = ev.getActiveTicketOptions();
        if (options.isEmpty()) {
            showError("No ticket types", "This event has no active ticket types.");
            showCustomerDashboard();
            return;
        }

        final Event.TicketOption[] selectedOption = {options.get(0)};
        final VBox[] selectedBoxRef = {null};

        for (Event.TicketOption option : options) {
            VBox optionBox = createTicketTypeBox(
                    option.getName(),
                    option.getPriceText(),
                    option == options.get(0)
            );

            if (option == options.get(0)) {
                selectedBoxRef[0] = optionBox;
            }

            optionBox.setOnMouseClicked(e -> {
                if (selectedBoxRef[0] != null) {
                    selectedBoxRef[0].getStyleClass().removeAll("ticket-type-box-active", "ticket-type-box");
                    selectedBoxRef[0].getStyleClass().add("ticket-type-box");
                }

                optionBox.getStyleClass().removeAll("ticket-type-box-active", "ticket-type-box");
                optionBox.getStyleClass().add("ticket-type-box-active");

                selectedBoxRef[0] = optionBox;
                selectedOption[0] = option;
            });

            typesBox.getChildren().add(optionBox);
        }

        Label qtyTitle = new Label("Quantity");
        qtyTitle.getStyleClass().add("form-label");

        HBox qtyBox = new HBox(15);
        qtyBox.setAlignment(Pos.CENTER_LEFT);

        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("qty-btn");

        Label qtyLbl = new Label("1");
        qtyLbl.setStyle("-fx-font-size: 16px;");

        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("qty-btn");

        qtyBox.getChildren().addAll(minusBtn, qtyLbl, plusBtn);

        Label detailsTitle = new Label("Extra Details");
        detailsTitle.getStyleClass().add("form-label");

        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Optional notes for the ticket...");
        detailsArea.setPrefRowCount(3);

        Label emailTitle = new Label("Customer Email");
        emailTitle.getStyleClass().add("form-label");

        TextField emailField = new TextField();
        emailField.setPromptText("example@email.com");

        Button confirmBtn = new Button("Confirm Purchase");
        confirmBtn.getStyleClass().add("primary-btn");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);

        selectionCard.getChildren().addAll(
                typeTitle,
                typesBox,
                qtyTitle,
                qtyBox,
                detailsTitle,
                detailsArea,
                emailTitle,
                emailField,
                confirmBtn
        );

        final int[] qty = {1};

        plusBtn.setOnAction(e -> {
            qty[0]++;
            qtyLbl.setText(String.valueOf(qty[0]));
        });

        minusBtn.setOnAction(e -> {
            if (qty[0] > 1) {
                qty[0]--;
                qtyLbl.setText(String.valueOf(qty[0]));
            }
        });

        confirmBtn.setOnAction(e -> {
            String email = emailField.getText().trim();

            if (!email.isEmpty() && !ticketController.isValidEmail(email)) {
                showError("Invalid Email", "Please enter a valid email address.");
                return;
            }

            List<Ticket> generatedTickets = ticketController.generateEntryTickets(
                    ev,
                    selectedOption[0],
                    qty[0],
                    detailsArea.getText().trim()
            );

            StringBuilder msg = new StringBuilder();
            msg.append("Generated ").append(generatedTickets.size()).append(" ticket(s)\n\n");

            for (int i = 0; i < generatedTickets.size(); i++) {
                Ticket t = generatedTickets.get(i);
                msg.append("Ticket ").append(i + 1).append(":\n");
                msg.append("ID: ").append(t.getTicketId()).append("\n");
                msg.append("Code: ").append(t.getSecureCode()).append("\n");
                msg.append("Type: ").append(t.getTicketName()).append("\n");
                msg.append("Value: ").append(t.getValueText()).append("\n\n");
            }

            showInfo("Purchase confirmed", msg.toString().trim());
            showCustomerDashboard();
        });

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
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add(isActive ? "ticket-type-box-active" : "ticket-type-box");

        Label t = new Label(type);
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A202C;");

        Label p = new Label(price);
        p.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 12px;");

        box.getChildren().addAll(t, p);
        return box;
    }

    private HBox createCustomerTopBar(String btnText,
                                      javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("dark-top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 10, 20));

        VBox logoBox = new VBox();

        Label logo1 = new Label("SEA\nErhvervsakademi");
        logo1.getStyleClass().add("top-bar-logo");

        Label logo2 = new Label("EASV Tickets - Customer Portal");
        logo2.getStyleClass().add("top-bar-sub");

        logoBox.getChildren().addAll(logo1, logo2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btn = new Button(btnText);
        btn.getStyleClass().add("secondary-btn-dark");
        btn.setOnAction(action);

        topBar.getChildren().addAll(logoBox, spacer, btn);
        return topBar;
    }

    // ==========================================
    // SHARED DASHBOARD CONTENT BUILDER
    // ==========================================
    private VBox createDashboardContent(String titleText, String viewMode) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(titleText);
        title.getStyleClass().add("page-title");
        headerBox.getChildren().add(title);

        TextField searchBar = new TextField();
        searchBar.setPromptText("🔍 Search events...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setMaxWidth(400);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        grid.setPrefWrapLength(1000);

        if ("COORD_EVENTS".equals(viewMode)) {
            Button createBtn = new Button("＋ Create Event");
            createBtn.getStyleClass().add("primary-btn");
            createBtn.setPrefWidth(1000);
            createBtn.setOnAction(e -> {
                int next = eventController.getAllEvents().size() + 1;

                Event event = new Event(
                        "New Event " + next,
                        "01 Jan 2027 at 12:00",
                        "EASV Campus, Esbjerg",
                        "New event created from coordinator dashboard",
                        "Available"
                );

                event.addTicketOption(new Event.TicketOption("Standard", "Regular ticket", 100));
                event.addTicketOption(new Event.TicketOption("VIP", "VIP access", 225));
                event.addTicketOption(new Event.TicketOption("Food Included", "Entry + meal", 175));

                eventController.createEvent(event);
                showCoordinatorDashboard("Events");
            });
            content.getChildren().add(createBtn);
        }

        for (Event ev : eventController.getAllEvents()) {
            grid.getChildren().add(buildDynamicCard(ev, viewMode));
        }

        content.getChildren().addAll(headerBox, searchBar, grid);
        return content;
    }

    private VBox buildDynamicCard(Event ev, String viewMode) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");

        HBox top = new HBox();

        Label tLbl = new Label(ev.getTitle());
        tLbl.getStyleClass().add("card-title");

        Label sLbl = new Label(ev.getStatus());
        sLbl.getStyleClass().add(
                "Available".equals(ev.getStatus()) ? "status-avail" : "status-fast"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        top.getChildren().addAll(tLbl, spacer, sLbl);

        Label dLbl = new Label("🕒 " + ev.getDate());
        dLbl.getStyleClass().add("card-text");

        Label lLbl = new Label("📍 " + ev.getLocation());
        lLbl.getStyleClass().add("card-text");

        Label nHead = new Label("Notes");
        nHead.getStyleClass().add("notes-head");

        Label nLbl = new Label(ev.getNotes());
        nLbl.getStyleClass().add("card-text");

        Label priceLbl = new Label(ev.getStartingPriceText());
        priceLbl.getStyleClass().add("price-text");

        Label optionsLbl = new Label(buildTicketOptionsSummary(ev));
        optionsLbl.getStyleClass().add("card-text");

        card.getChildren().addAll(top, dLbl, lLbl, nHead, nLbl, new Separator(), priceLbl, optionsLbl);

        if ("CUSTOMER".equals(viewMode)) {
            Button buyBtn = new Button("Buy Ticket");
            buyBtn.getStyleClass().add("primary-btn");
            buyBtn.setMaxWidth(Double.MAX_VALUE);
            buyBtn.setOnAction(e -> showBuyTicketScreen(ev));

            card.getChildren().add(buyBtn);

        } else if ("ADMIN_EVENTS".equals(viewMode) || "COORD_EVENTS".equals(viewMode)) {
            Button delBtn = new Button("🗑 Delete Event");
            delBtn.getStyleClass().add("danger-btn");
            delBtn.setMaxWidth(Double.MAX_VALUE);
            delBtn.setOnAction(e -> {
                eventController.deleteEvent(ev);

                if ("ADMIN_EVENTS".equals(viewMode)) {
                    showAdminDashboard("Events");
                } else {
                    showCoordinatorDashboard("Events");
                }
            });

            card.getChildren().add(delBtn);

        } else if ("COORD_ACCESS".equals(viewMode)) {
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
            assignBtn.setOnAction(e -> {
                List<User> coordinators = userController.getUsersByRole("Event Coordinator");
                if (!coordinators.isEmpty()) {
                    String coordinatorName = coordinators.get(0).getName();
                    eventController.assignCoordinatorToEvent(ev, coordinatorName);
                    showCoordinatorDashboard("Manage Access");
                }
            });

            Button delBtn = new Button("🗑 Delete Event");
            delBtn.getStyleClass().add("danger-btn");
            delBtn.setMaxWidth(Double.MAX_VALUE);
            delBtn.setOnAction(e -> {
                eventController.deleteEvent(ev);
                showCoordinatorDashboard("Manage Access");
            });

            card.getChildren().addAll(asgnHead, pillBox, assignBtn, delBtn);
        }

        return card;
    }

    private String buildTicketOptionsSummary(Event event) {
        List<Event.TicketOption> options = event.getActiveTicketOptions();
        if (options.isEmpty()) {
            return "No active ticket types";
        }

        StringBuilder sb = new StringBuilder("Types: ");
        for (int i = 0; i < options.size(); i++) {
            sb.append(options.get(i).getName());
            if (i < options.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private VBox createHeader(String aT, String aS, String pT, String pS) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Label l1 = new Label("SEA");
        l1.getStyleClass().add("logo-large");

        Label l2 = new Label("Erhvervsakademi");
        l2.getStyleClass().add("logo-small");

        Label l3 = new Label(aT);
        l3.getStyleClass().add("app-title");

        box.getChildren().addAll(l1, l2, l3);

        if (!aS.isEmpty()) {
            Label l4 = new Label(aS);
            l4.getStyleClass().add("app-subtitle");
            box.getChildren().add(l4);
        }

        if (!pT.isEmpty()) {
            VBox spacing = new VBox();
            spacing.setMinHeight(20);

            Label pt = new Label(pT);
            pt.getStyleClass().add("page-title-center");

            Label ps = new Label(pS);
            ps.getStyleClass().add("page-subtitle-center");

            box.getChildren().addAll(spacing, pt, ps);
        }

        return box;
    }

    private VBox createPortalCard(String i, String t, String s) {
        VBox c = new VBox(10);
        c.setAlignment(Pos.CENTER);
        c.getStyleClass().add("portal-card");

        Label ic = new Label(i);
        ic.getStyleClass().add("portal-icon");

        Label tL = new Label(t);
        tL.getStyleClass().add("card-title");

        Label sL = new Label(s);
        sL.getStyleClass().add("card-subtitle");

        c.getChildren().addAll(ic, tL, sL);
        return c;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(window);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(window);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}