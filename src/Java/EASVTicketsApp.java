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

public class EASVTicketsApp extends Application {

    private Stage window;
    private Scene mainScene;
    private StackPane rootPane;

    // Dummy data structure to hold event info for the GUI
    private static class EventData {
        String title, date, location, notes, price, status;
        String[] coordinators;
        public EventData(String t, String d, String l, String n, String p, String s, String[] c) {
            title = t; date = d; location = l; notes = n; price = p; status = s; coordinators = c;
        }
    }

    private List<EventData> eventsList;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("EASV Ticket Management System");

        // Initialize dummy data
        eventsList = new ArrayList<>();
        eventsList.add(new EventData("EASV Graduation Ceremony 2026", "20 Jun 2026 at 14:00", "EASV Campus, Esbjerg", "Annual graduation ceremony for EASV students", "Free", "Available", new String[]{"Event Coordinator 1", "Event Coordinator 2"}));
        eventsList.add(new EventData("Tech Innovation Summit", "15 Jul 2026 at 09:00", "Innovation Hub, Esbjerg", "Annual technology and innovation conference", "150 DKK", "Selling Fast", new String[]{"Event Coordinator 3"}));
        eventsList.add(new EventData("Danish Business Networking", "22 Aug 2026 at 18:00", "Copenhagen Convention Center", "Business networking event for professionals", "500 DKK", "Available", new String[]{"Event Coordinator 4", "Event Coordinator 5"}));

        rootPane = new StackPane();
        mainScene = new Scene(rootPane, 1200, 800);
        mainScene.getStylesheets().add(getClass().getResource("/css/easv-style.css").toExternalForm());

        showPortalSelection();

        window.setScene(mainScene);
        window.show();
    }

    // ==========================================
    // PORTAL & LOGIN
    // ==========================================
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

    private void showBuyTicketScreen(EventData ev) {
        BorderPane layout = new BorderPane(); layout.getStyleClass().add("main-bg");
        layout.setTop(createCustomerTopBar("🏠 Back to Portal Selection", e -> showPortalSelection()));

        VBox content = new VBox(20);
        content.setMaxWidth(650);

        Label pageTitle = new Label("Buy Ticket"); pageTitle.getStyleClass().add("page-title");

        VBox eventCard = new VBox(10); eventCard.getStyleClass().add("buy-ticket-card");
        HBox top = new HBox();
        Label tLbl = new Label(ev.title); tLbl.getStyleClass().add("card-title-large");
        Label sLbl = new Label(ev.status); sLbl.getStyleClass().add(ev.status.equals("Available") ? "status-avail" : "status-fast");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(tLbl, spacer, sLbl);
        Label dLbl = new Label("🕒 " + ev.date); dLbl.getStyleClass().add("card-text");
        Label lLbl = new Label("📍 " + ev.location); lLbl.getStyleClass().add("card-text");
        Label nLbl = new Label(ev.notes); nLbl.getStyleClass().add("card-text"); nLbl.setPadding(new Insets(10,0,0,0));
        eventCard.getChildren().addAll(top, dLbl, lLbl, new Separator(), nLbl);

        VBox selectionCard = new VBox(20); selectionCard.getStyleClass().add("buy-ticket-card");
        Label typeTitle = new Label("Select Ticket Type"); typeTitle.getStyleClass().add("card-title");

        HBox typesBox = new HBox(15);
        VBox type1 = createTicketTypeBox("Standard", ev.price, true);
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
        Label priceLbl = new Label(ev.price); priceLbl.getStyleClass().add("price-text-large");
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
            Button createBtn = new Button("＋ Create Event");
            createBtn.getStyleClass().add("primary-btn");
            createBtn.setPrefWidth(1000);
            content.getChildren().add(createBtn);
        }

        for (EventData ev : eventsList) {
            grid.getChildren().add(buildDynamicCard(ev, viewMode));
        }

        content.getChildren().addAll(headerBox, searchBar, grid);
        return content;
    }

    private VBox buildDynamicCard(EventData ev, String viewMode) {
        VBox card = new VBox(10); card.getStyleClass().add("event-card");

        HBox top = new HBox();
        Label tLbl = new Label(ev.title); tLbl.getStyleClass().add("card-title");
        Label sLbl = new Label(ev.status); sLbl.getStyleClass().add(ev.status.equals("Available") ? "status-avail" : "status-fast");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(tLbl, spacer, sLbl);

        Label dLbl = new Label("🕒 " + ev.date); dLbl.getStyleClass().add("card-text");
        Label lLbl = new Label("📍 " + ev.location); lLbl.getStyleClass().add("card-text");
        Label nHead = new Label("Notes"); nHead.getStyleClass().add("notes-head");
        Label nLbl = new Label(ev.notes); nLbl.getStyleClass().add("card-text");

        card.getChildren().addAll(top, dLbl, lLbl, nHead, nLbl, new Separator());

        if (viewMode.equals("CUSTOMER")) {
            Label pLbl = new Label(ev.price); pLbl.getStyleClass().add("price-text");
            Button buyBtn = new Button("Buy Ticket"); buyBtn.getStyleClass().add("primary-btn"); buyBtn.setMaxWidth(Double.MAX_VALUE);
            buyBtn.setOnAction(e -> showBuyTicketScreen(ev));
            card.getChildren().addAll(pLbl, buyBtn);
        } else if (viewMode.equals("ADMIN_EVENTS") || viewMode.equals("COORD_EVENTS")) {
            Label pLbl = new Label(ev.price); pLbl.getStyleClass().add("price-text");
            Button delBtn = new Button("🗑 Delete Event"); delBtn.getStyleClass().add("danger-btn"); delBtn.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().addAll(pLbl, delBtn);
        } else if (viewMode.equals("COORD_ACCESS")) {
            Label asgnHead = new Label("Assigned Coordinators"); asgnHead.getStyleClass().add("notes-head");
            FlowPane pillBox = new FlowPane(5, 5);
            for (String c : ev.coordinators) {
                Label pill = new Label(c); pill.getStyleClass().add("coord-pill");
                pillBox.getChildren().add(pill);
            }
            Button assignBtn = new Button("👥 Assign Access"); assignBtn.getStyleClass().add("primary-btn"); assignBtn.setMaxWidth(Double.MAX_VALUE);
            Button delBtn = new Button("🗑 Delete Event"); delBtn.getStyleClass().add("danger-btn"); delBtn.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().addAll(asgnHead, pillBox, assignBtn, delBtn);
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