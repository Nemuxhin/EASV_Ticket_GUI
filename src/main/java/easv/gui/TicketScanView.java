package easv.gui;

import easv.be.Event;
import easv.be.Ticket;
import easv.bll.QrScannerService;
import easv.bll.TicketRedemptionService;
import easv.bll.TicketScanResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class TicketScanView {

    private final MainView mainView;
    private final Event event;
    private final QrScannerService scannerService;
    private final TicketRedemptionService redemptionService;

    public TicketScanView(MainView mainView, Event event) {
        this.mainView = mainView;
        this.event = event;
        this.scannerService = new QrScannerService();
        this.redemptionService = new TicketRedemptionService();
    }

    public Parent getView() {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24));
        page.getStyleClass().add("main-bg");

        Label title = new Label("Scan Ticket");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Event: " + (event == null ? "-" : event.getTitle()));
        subtitle.getStyleClass().add("card-text");

        TextField tokenField = new TextField();
        tokenField.setPromptText("Scanned token appears here, or paste it manually");
        tokenField.getStyleClass().add("input-field");

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(10);

        Button chooseImageButton = new Button("Choose QR Image");
        chooseImageButton.getStyleClass().add("primary-btn");
        chooseImageButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose QR / Barcode Image");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
            );

            File file = chooser.showOpenDialog(page.getScene().getWindow());
            if (file == null) {
                return;
            }

            try {
                String token = scannerService.decodeToken(file);
                tokenField.setText(token);
                TicketScanResult result = redemptionService.redeem(mainView.getCurrentUser(), event, token);
                resultArea.setText(buildResultText(result));
            } catch (Exception ex) {
                resultArea.setText("Scan failed:\n" + ex.getMessage());
            }
        });

        Button redeemButton = new Button("Redeem Token");
        redeemButton.getStyleClass().add("primary-btn");
        redeemButton.setOnAction(e -> {
            TicketScanResult result = redemptionService.redeem(mainView.getCurrentUser(), event, tokenField.getText());
            resultArea.setText(buildResultText(result));
        });

        Button backButton = new Button("Back to Events");
        backButton.getStyleClass().add("secondary-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        HBox actionRow = new HBox(12, chooseImageButton, redeemButton, backButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        page.getChildren().addAll(
                title,
                subtitle,
                tokenField,
                actionRow,
                resultArea
        );

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private String buildResultText(TicketScanResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append(result.title()).append("\n\n");
        builder.append(result.message());

        Ticket ticket = result.ticket();
        if (ticket != null) {
            builder.append("\n\nTicket ID: ").append(ticket.getTicketId());
            builder.append("\nType: ").append(ticket.getTicketType());
            builder.append("\nEvent: ").append(ticket.getEventTitle());
            builder.append("\nUsed: ").append(ticket.isUsed() ? "Yes" : "No");
            builder.append("\nActive: ").append(ticket.isActive() ? "Yes" : "No");

            if (ticket.getCustomer() != null) {
                builder.append("\nCustomer: ").append(ticket.getCustomer().getName());
                builder.append("\nEmail: ").append(ticket.getCustomer().getEmail());
            }
        }

        return builder.toString();
    }
}
