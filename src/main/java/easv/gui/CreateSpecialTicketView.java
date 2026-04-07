package easv.gui;

import easv.be.Ticket;
import easv.bll.BarcodeGenerator;
import easv.bll.QrCodeGenerator;
import easv.controller.TicketController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateSpecialTicketView {

    private final MainView mainView;
    private final TicketController ticketController;
    private final QrCodeGenerator qrCodeGenerator;
    private final BarcodeGenerator barcodeGenerator;

    public CreateSpecialTicketView(MainView mainView) {
        this.mainView = mainView;
        this.ticketController = new TicketController();
        this.qrCodeGenerator = new QrCodeGenerator();
        this.barcodeGenerator = new BarcodeGenerator();
    }

    public Parent getView() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24, 24, 24, 24));
        page.getStyleClass().add("main-bg");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Create Special Ticket");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("← Back to Events");
        backButton.getStyleClass().add("primary-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        topBar.getChildren().addAll(title, spacer, backButton);

        Label subtitle = new Label("Generate custom special tickets with unique benefits for your events.");
        subtitle.getStyleClass().add("card-text");

        TextField ticketNameField = new TextField();
        ticketNameField.setPromptText("e.g., One Free Beer, VIP Backstage Pass, Early Entry");
        ticketNameField.getStyleClass().add("input-field");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("e.g., Redeemable for one complimentary beer at the venue bar");
        descriptionArea.getStyleClass().add("input-field");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);

        TextField benefitField = new TextField();
        benefitField.setPromptText("e.g., One free drink, Priority entry, 50% discount");
        benefitField.getStyleClass().add("input-field");

        TextField priceField = new TextField();
        priceField.setPromptText("e.g., 0 or 25");
        priceField.getStyleClass().add("input-field");

        TextField quantityField = new TextField();
        quantityField.setPromptText("e.g., 50");
        quantityField.getStyleClass().add("input-field");

        ToggleGroup appliesToGroup = new ToggleGroup();

        RadioButton thisEventOnlyRadio = new RadioButton("This event only");
        thisEventOnlyRadio.setToggleGroup(appliesToGroup);
        thisEventOnlyRadio.setSelected(true);

        RadioButton allEventsRadio = new RadioButton("All events");
        allEventsRadio.setToggleGroup(appliesToGroup);

        VBox appliesBox = new VBox(10, thisEventOnlyRadio, allEventsRadio);

        ComboBox<String> eventComboBox = new ComboBox<>();
        eventComboBox.setEditable(true);
        eventComboBox.setPromptText("Select an event");
        eventComboBox.setMaxWidth(Double.MAX_VALUE);

        DatePicker validUntilPicker = new DatePicker();
        validUntilPicker.setPromptText("dd/mm/yyyy");
        validUntilPicker.setMaxWidth(Double.MAX_VALUE);

        Label previewType = new Label("SPECIAL TICKET");
        previewType.getStyleClass().add("card-text");

        Label previewBadge = new Label("One-Time Use");
        previewBadge.getStyleClass().add("summary-value-highlight");

        HBox previewTop = new HBox(10, previewType, grow(), previewBadge);
        previewTop.setAlignment(Pos.CENTER_LEFT);

        Label previewName = new Label("Special Ticket Name");
        previewName.getStyleClass().add("page-title");

        Label previewValidForTitle = new Label("VALID FOR");
        previewValidForTitle.getStyleClass().add("card-text");

        Label previewValidFor = new Label("Select an event");
        previewValidFor.getStyleClass().add("card-text");

        Label previewPriceTitle = new Label("PRICE");
        previewPriceTitle.getStyleClass().add("card-text");

        Label previewPrice = new Label("0 DKK");
        previewPrice.getStyleClass().add("summary-value-highlight");

        Label previewBenefitTitle = new Label("BENEFIT");
        previewBenefitTitle.getStyleClass().add("card-text");

        Label previewBenefit = new Label("Custom benefit");
        previewBenefit.getStyleClass().add("card-text");
        previewBenefit.setWrapText(true);

        ImageView qrPreview = new ImageView();
        qrPreview.setFitWidth(110);
        qrPreview.setFitHeight(110);
        qrPreview.setPreserveRatio(true);

        ImageView barcodePreview = new ImageView();
        barcodePreview.setFitWidth(260);
        barcodePreview.setFitHeight(70);
        barcodePreview.setPreserveRatio(true);

        Label previewNotes = new Label("Redemption instructions will appear here.");
        previewNotes.getStyleClass().add("card-text");
        previewNotes.setWrapText(true);

        VBox previewCard = new VBox(
                14,
                previewTop,
                new Separator(),
                previewName,
                new Separator(),
                previewValidForTitle,
                previewValidFor,
                new Separator(),
                previewPriceTitle,
                previewPrice,
                new Separator(),
                previewBenefitTitle,
                previewBenefit,
                new Separator(),
                qrPreview,
                barcodePreview,
                previewNotes
        );
        previewCard.getStyleClass().add("event-card");
        previewCard.setPadding(new Insets(20));

        Runnable refreshPreview = () -> {
            String titleText = safeText(ticketNameField.getText(), "Special Ticket Name");
            String benefitText = safeText(benefitField.getText(), "Custom benefit");
            String descriptionText = safeText(descriptionArea.getText(), "Redemption instructions will appear here.");
            String eventText = allEventsRadio.isSelected()
                    ? "All events"
                    : safeText(getComboValue(eventComboBox), "Select an event");

            String priceText = formatPreviewPrice(priceField.getText());

            previewName.setText(titleText);
            previewBenefit.setText(benefitText);
            previewValidFor.setText(eventText);
            previewPrice.setText(priceText);

            String validUntilText = validUntilPicker.getValue() != null
                    ? " Valid until " + validUntilPicker.getValue() + "."
                    : "";

            previewNotes.setText(descriptionText + validUntilText);

            String previewTokenSource = titleText + "|" + eventText + "|" + priceText + "|" + benefitText;
            String previewToken = UUID.nameUUIDFromBytes(previewTokenSource.getBytes(StandardCharsets.UTF_8)).toString();

            qrPreview.setImage(bytesToImage(qrCodeGenerator.generateQrCode(previewToken)));
            barcodePreview.setImage(bytesToImage(barcodeGenerator.generateBarcode(previewToken)));
        };

        ticketNameField.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        descriptionArea.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        benefitField.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        priceField.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        quantityField.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        eventComboBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        eventComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        validUntilPicker.valueProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        appliesToGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
            eventComboBox.setDisable(allEventsRadio.isSelected());
            refreshPreview.run();
        });

        refreshPreview.run();

        Button generateButton = new Button("+ Generate Special Tickets");
        generateButton.getStyleClass().add("primary-btn");
        generateButton.setMaxWidth(Double.MAX_VALUE);
        generateButton.setOnAction(e -> handleGenerate(
                ticketNameField,
                descriptionArea,
                benefitField,
                priceField,
                quantityField,
                thisEventOnlyRadio,
                allEventsRadio,
                eventComboBox,
                validUntilPicker
        ));

        VBox leftCard = new VBox(
                14,
                sectionTitle("Ticket Details"),
                createField("Ticket Name *", ticketNameField),
                createField("Description", descriptionArea),
                createField("Ticket Value / Benefit", benefitField),
                buildTwoColumnFields(
                        createField("Price (DKK) *", priceField),
                        createField("Quantity to Generate *", quantityField)
                ),
                createField("Applies To", appliesBox),
                createField("Event", eventComboBox),
                createField("Valid Until (Optional)", validUntilPicker),
                generateButton
        );
        leftCard.getStyleClass().add("event-card");
        leftCard.setPadding(new Insets(20));

        VBox rightCard = new VBox(
                14,
                sectionTitle("Live Preview"),
                styleLabel("Preview of the generated special ticket", "card-text"),
                previewCard
        );
        rightCard.getStyleClass().add("event-card");
        rightCard.setPadding(new Insets(20));

        HBox content = new HBox(20, leftCard, rightCard);
        HBox.setHgrow(leftCard, Priority.ALWAYS);
        HBox.setHgrow(rightCard, Priority.ALWAYS);

        page.getChildren().addAll(topBar, subtitle, content);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        return scrollPane;
    }

    private void handleGenerate(TextField ticketNameField,
                                TextArea descriptionArea,
                                TextField benefitField,
                                TextField priceField,
                                TextField quantityField,
                                RadioButton thisEventOnlyRadio,
                                RadioButton allEventsRadio,
                                ComboBox<String> eventComboBox,
                                DatePicker validUntilPicker) {
        String ticketName = ticketNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String benefit = benefitField.getText().trim();
        String eventTitle = getComboValue(eventComboBox);
        boolean validForAllEvents = allEventsRadio.isSelected();

        if (ticketName.isBlank()) {
            AlertHelper.showError("Invalid Special Ticket", "Ticket name is required.");
            return;
        }

        if (!validForAllEvents && eventTitle.isBlank()) {
            AlertHelper.showError("Invalid Special Ticket", "Please select or type an event.");
            return;
        }

        int quantity;
        double priceValue;

        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity < 1) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            AlertHelper.showError("Invalid Special Ticket", "Quantity must be a whole number greater than 0.");
            return;
        }

        try {
            String rawPrice = priceField.getText().trim().replace(",", ".");
            priceValue = Double.parseDouble(rawPrice);
            if (priceValue < 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            AlertHelper.showError("Invalid Special Ticket", "Price must be a number 0 or greater.");
            return;
        }

        String ticketDescription = benefit.isBlank() ? description : benefit;
        if (ticketDescription.isBlank()) {
            ticketDescription = "Special ticket";
        }

        String eventNotes = buildEventNotes(description, validUntilPicker.getValue());

        List<Ticket> tickets = ticketController.createSpecialTickets(
                validForAllEvents ? null : eventTitle,
                "",
                "",
                "",
                "",
                eventNotes,
                ticketName,
                ticketDescription,
                formatPrice(priceValue),
                validForAllEvents,
                quantity
        );

        showSuccess(tickets, ticketName, validForAllEvents ? "All events" : eventTitle);
    }

    private void showSuccess(List<Ticket> tickets, String ticketName, String scope) {
        if (tickets == null || tickets.isEmpty()) {
            AlertHelper.showError("Generation Error", "No special tickets were created.");
            return;
        }

        Ticket firstTicket = tickets.get(0);

        VBox page = new VBox(18);
        page.setPadding(new Insets(24));
        page.setAlignment(Pos.CENTER);
        page.getStyleClass().add("main-bg");

        VBox card = new VBox(14);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(24));
        card.setMaxWidth(860);
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Special Tickets Generated");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label(tickets.size() + " \"" + ticketName + "\" tickets were created.");
        subtitle.getStyleClass().add("card-text");

        ImageView qrView = new ImageView(new Image(new ByteArrayInputStream(firstTicket.getQrImage())));
        qrView.setFitWidth(180);
        qrView.setFitHeight(180);
        qrView.setPreserveRatio(true);

        ImageView barcodeView = new ImageView(new Image(new ByteArrayInputStream(firstTicket.getBarcodeImage())));
        barcodeView.setFitWidth(340);
        barcodeView.setFitHeight(90);
        barcodeView.setPreserveRatio(true);

        VBox idsBox = new VBox(8);
        idsBox.setAlignment(Pos.CENTER_LEFT);
        idsBox.setMaxWidth(600);

        for (int i = 0; i < tickets.size(); i++) {
            Label idLabel = new Label("Ticket " + (i + 1) + ": " + tickets.get(i).getTicketId());
            idLabel.getStyleClass().add("card-text");
            idsBox.getChildren().add(idLabel);
        }

        Label details = new Label(
                "Ticket Type: " + ticketName + "\n" +
                        "Valid For: " + scope + "\n" +
                        "Quantity: " + tickets.size() + "\n" +
                        "Price: " + firstTicket.getPrice()
        );
        details.getStyleClass().add("card-text");
        details.setWrapText(true);
        details.setMaxWidth(540);

        Button backButton = new Button("Back to Events");
        backButton.getStyleClass().add("primary-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        card.getChildren().addAll(
                title,
                subtitle,
                details,
                new Label("First Ticket QR Code"),
                qrView,
                new Label("First Ticket Barcode"),
                barcodeView,
                new Separator(),
                sectionTitle("Generated Ticket IDs"),
                idsBox,
                backButton
        );

        page.getChildren().add(card);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        mainView.setContent(scrollPane);
    }

    private HBox buildTwoColumnFields(VBox left, VBox right) {
        HBox row = new HBox(14, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        return row;
    }

    private VBox createField(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(6);

        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        box.getChildren().addAll(label, field);
        return box;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("notes-head");
        return label;
    }

    private Label styleLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        return label;
    }

    private Region grow() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private String getComboValue(ComboBox<String> comboBox) {
        if (comboBox.getValue() != null && !comboBox.getValue().isBlank()) {
            return comboBox.getValue().trim();
        }

        if (comboBox.getEditor() != null && comboBox.getEditor().getText() != null) {
            return comboBox.getEditor().getText().trim();
        }

        return "";
    }

    private String buildEventNotes(String description, LocalDate validUntil) {
        StringBuilder builder = new StringBuilder();

        if (description != null && !description.isBlank()) {
            builder.append(description.trim());
        }

        if (validUntil != null) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append("Valid until: ").append(validUntil);
        }

        return builder.toString();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String formatPreviewPrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return "0 DKK";
        }

        try {
            double value = Double.parseDouble(rawPrice.trim().replace(",", "."));
            return formatPrice(value);
        } catch (Exception ex) {
            return "0 DKK";
        }
    }

    private String formatPrice(double amount) {
        if (amount == Math.floor(amount)) {
            return String.format("%.0f DKK", amount);
        }
        return String.format("%.2f DKK", amount);
    }

    private Image bytesToImage(byte[] bytes) {
        return new Image(new ByteArrayInputStream(bytes));
    }
}