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

public class EASVTicketsApp extends Application {
    private EventController eventController = new EventController();
    private StackPane rootPane = new StackPane();

    @Override
    public void start(Stage primaryStage) {
        showPortalSelection();
        Scene mainScene = new Scene(rootPane, 1200, 800);
        mainScene.getStylesheets().add(getClass().getResource("/CSS/easv-style.css").toExternalForm());
        primaryStage.setTitle("EASV Ticket Management System");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private void showPortalSelection() {
        VBox layout = new VBox(30); layout.setAlignment(Pos.CENTER); layout.getStyleClass().add("main-bg");
        VBox header = ViewFactory.createHeader("EASV Tickets", "Management System", "Select Portal", "Choose Access");

        HBox cards = new HBox(20); cards.setAlignment(Pos.CENTER);
        VBox admin = ViewFactory.createPortalCard("👤", "Admin", "Coordinators");
        admin.setOnMouseClicked(e -> showDashboard("Admin", "Coordinators"));

        VBox customer = ViewFactory.createPortalCard("🛍️", "Customer", "Buy Tickets");
        customer.setOnMouseClicked(e -> showCustomerDashboard());

        cards.getChildren().addAll(admin, customer);
        layout.getChildren().addAll(header, cards);
        rootPane.getChildren().setAll(layout);
    }

    private void showDashboard(String role, String activeTab) {
        BorderPane layout = new BorderPane();
        VBox sidebar = new VBox(10); sidebar.getStyleClass().add("sidebar"); sidebar.setPadding(new Insets(20));
        sidebar.getChildren().add(new Label(role + " Portal"){{getStyleClass().add("sidebar-logo");}});

        Button logout = new Button("Logout"); logout.setOnAction(e -> showPortalSelection());
        sidebar.getChildren().addAll(new Region(){{VBox.setVgrow(this, Priority.ALWAYS);}}, logout);

        FlowPane grid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        for (Event ev : eventController.getEvents()) {
            grid.getChildren().add(ViewFactory.buildDynamicCard(ev, "ADMIN", null));
        }

        layout.setLeft(sidebar);
        layout.setCenter(new ScrollPane(grid){{setFitToWidth(true);}});
        rootPane.getChildren().setAll(layout);
    }

    private void showCustomerDashboard() {
        VBox content = new VBox(20); content.setPadding(new Insets(30));
        FlowPane grid = new FlowPane(20, 20);
        for (Event ev : eventController.getEvents()) {
            grid.getChildren().add(ViewFactory.buildDynamicCard(ev, "CUSTOMER", () -> showBuyTicket(ev)));
        }
        Button back = new Button("Back"); back.setOnAction(e -> showPortalSelection());
        content.getChildren().addAll(back, new Label("Available Events"){{getStyleClass().add("page-title");}}, grid);
        rootPane.getChildren().setAll(new ScrollPane(content));
    }

    private void showBuyTicket(Event ev) {
        // Restoring Quantity Logic
        final int[] qty = {1};
        Label qtyLbl = new Label("1");
        Button plus = new Button("+"); plus.setOnAction(e -> { qty[0]++; qtyLbl.setText(""+qty[0]); });

        VBox buyBox = new VBox(20, new Label("Buy: " + ev.getTitle()), new HBox(10, qtyLbl, plus), new Button("Confirm"){{setOnAction(e -> showCustomerDashboard());}});
        buyBox.setAlignment(Pos.CENTER);
        rootPane.getChildren().setAll(buyBox);
    }

    public static void main(String[] args) { launch(args); }
}