package easv.gui;

import easv.be.Ticket;
import easv.controller.TicketController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SoldTicketsView {
    private final MainView mainView;
    private final TicketController ticketController;
    private final String returnRole;

    public SoldTicketsView(MainView mainView, String returnRole) {
        this.mainView = mainView;
        this.ticketController = new TicketController();
        this.returnRole = returnRole;
    }

    public Parent getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 50, 30, 50));
        content.getStyleClass().add("main-bg");

        FlowPane ticketGrid = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        ticketGrid.setPrefWrapLength(1000);

        TextField tokenField = new TextField();
        tokenField.setPromptText("Paste ticket QR/barcode token");
        tokenField.getStyleClass().add("search-bar");
        tokenField.setPrefWidth(520);

        Button validateButton = new Button("Validate Ticket");
        validateButton.getStyleClass().add("primary-btn");
        validateButton.setOnAction(e -> validateTicket(tokenField.getText().trim(), ticketGrid));

        HBox toolbar = new HBox(12, tokenField, validateButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        populateSoldTickets(ticketGrid);

        content.getChildren().addAll(
                buildTopBar(),
                styleLabel("Use this section to validate sold tickets and help customers if they lose their ticket.", "card-text"),
                toolbar,
                ticketGrid
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        return scrollPane;
    }

    private HBox buildTopBar() {
        Label title = new Label("Sold Tickets");
        title.getStyleClass().add("page-title");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("secondary-btn");
        backButton.setOnAction(e -> goBack());

        HBox topBar = new HBox(12, title, grow(), backButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        return topBar;
    }

    // (Samu) A sold ticket is valid only once; after validation it becomes used.
    private void validateTicket(String token, FlowPane ticketGrid) {
        if (token.isBlank()) {
            AlertHelper.showError("Invalid Ticket", "Please enter a ticket token.");
            return;
        }

        boolean valid = ticketController.isTicketValid(token);
        if (!valid) {
            AlertHelper.showError("Invalid Ticket", "This ticket is not active, or it has already been used.");
            return;
        }

        boolean updated = ticketController.markTicketAsUsed(token);
        if (!updated) {
            AlertHelper.showError("Validation Failed", "The ticket could not be marked as used.");
            return;
        }

        AlertHelper.showInfo("Ticket Validated", "The ticket is valid and has now been marked as used.");
        populateSoldTickets(ticketGrid);
    }

    private void populateSoldTickets(FlowPane ticketGrid) {
        ticketGrid.getChildren().clear();
        boolean hasSoldTickets = false;

        for (Ticket ticket : ticketController.getAllTickets()) {
            if (!ticket.hasCustomer()) {
                continue;
            }

            hasSoldTickets = true;
            ticketGrid.getChildren().add(createTicketCard(ticket));
        }

        if (!hasSoldTickets) {
            Label empty = styleLabel("No sold tickets yet.", "card-text");
            ticketGrid.getChildren().add(empty);
        }
    }

    private VBox createTicketCard(Ticket ticket) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");

        Label title = styleLabel(ticket.getTicketType(), "card-title");
        Label event = styleLabel("Event: " + safeText(ticket.getEventTitle()), "card-text");
        Label customer = styleLabel("Customer: " + safeText(ticket.getCustomer().getName()), "card-text");
        Label email = styleLabel("Email: " + safeText(ticket.getCustomer().getEmail()), "card-text");
        Label ticketId = styleLabel("Ticket ID: " + safeText(ticket.getTicketId()), "card-text");
        Label token = styleLabel("Token: " + safeText(ticket.getSecureToken()), "card-text");
        Label status = styleLabel(ticket.isUsed() ? "Status: Used" : "Status: Active", ticket.isUsed() ? "status-fast" : "status-avail");

        card.getChildren().addAll(title, event, customer, email, new Separator(), ticketId, token, status);
        return card;
    }

    private void goBack() {
        if ("Admin".equalsIgnoreCase(returnRole)) {
            mainView.showAdminDashboard("Events");
            return;
        }

        mainView.showCoordinatorDashboard("Events");
    }

    private Label styleLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        return label;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Region grow() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }
}
