package easv.gui;

import easv.be.Event;
import easv.be.SpecialTicketRecord;
import easv.be.Ticket;
import easv.bll.BarcodeGenerator;
import easv.bll.QrCodeGenerator;
import easv.controller.EventController;
import easv.controller.TicketController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreateSpecialTicketView {

    private final MainView mainView;
    private final EventController eventController;
    private final TicketController ticketController;
    private final QrCodeGenerator qrCodeGenerator;
    private final BarcodeGenerator barcodeGenerator;

    public CreateSpecialTicketView(MainView mainView) {
        this.mainView = mainView;
        this.eventController = new EventController();
        this.ticketController = new TicketController();
        this.qrCodeGenerator = new QrCodeGenerator();
        this.barcodeGenerator = new BarcodeGenerator();
    }

    public Parent getView() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));
        page.getStyleClass().add("main-bg");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Create Special Ticket");
        title.getStyleClass().add("page-title");

        Button backButton = new Button("< Back to Events");
        backButton.getStyleClass().add("primary-btn");
        backButton.setOnAction(e -> mainView.showCoordinatorDashboard("Events"));

        topBar.getChildren().addAll(title, grow(), backButton);

        Label subtitle = new Label("Save special ticket templates here, then generate real one-time tickets from the saved cards below.");
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
        eventComboBox.setItems(FXCollections.observableArrayList(
                eventController.getEvents().stream().map(Event::getTitle).collect(Collectors.toList())
        ));
        eventComboBox.setPromptText("Select an event");
        eventComboBox.setMaxWidth(Double.MAX_VALUE);

        DatePicker validUntilPicker = new DatePicker();
        validUntilPicker.setPromptText("dd/mm/yyyy");
        validUntilPicker.setMaxWidth(Double.MAX_VALUE);
        validUntilPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });

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

        Label previewPrice = new Label("Free");
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
                    : safeText(eventComboBox.getValue(), "Select an event");

            previewName.setText(titleText);
            previewBenefit.setText(benefitText);
            previewValidFor.setText(eventText);
            previewPrice.setText(formatPreviewPrice(priceField.getText()));
            previewNotes.setText(buildPreviewNotes(descriptionText, validUntilPicker.getValue()));

            String previewTokenSource = titleText + "|" + eventText + "|" + benefitText + "|" + previewPrice.getText();
            String previewToken = UUID.nameUUIDFromBytes(previewTokenSource.getBytes(StandardCharsets.UTF_8)).toString();
            qrPreview.setImage(bytesToImage(qrCodeGenerator.generateQrCode(previewToken)));
            barcodePreview.setImage(bytesToImage(barcodeGenerator.generateBarcode(previewToken)));
        };

        ticketNameField.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        descriptionArea.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        benefitField.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        priceField.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        eventComboBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        validUntilPicker.valueProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        appliesToGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
            boolean allEvents = allEventsRadio.isSelected();
            eventComboBox.setDisable(allEvents);
            if (allEvents) {
                eventComboBox.setValue(null);
            }
            refreshPreview.run();
        });

        refreshPreview.run();

        Button saveButton = new Button("+ Save Special Ticket");
        saveButton.getStyleClass().add("primary-btn");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(e -> {
            boolean saved = handleSave(
                    ticketNameField,
                    descriptionArea,
                    benefitField,
                    priceField,
                    quantityField,
                    allEventsRadio.isSelected(),
                    eventComboBox,
                    validUntilPicker
            );

            if (saved) {
                mainView.showCoordinatorDashboard("Special Tickets");
            }
        });

        VBox leftCard = new VBox(
                14,
                sectionTitle("Ticket Details"),
                createField("Ticket Name *", ticketNameField),
                createField("Description", descriptionArea),
                createField("Ticket Value / Benefit", benefitField),
                buildTwoColumnFields(
                        createField("Price (DKK) *", priceField),
                createField("Quantity *", quantityField)
                ),
                createField("Applies To", appliesBox),
                createField("Event", eventComboBox),
                createField("Valid Until (Optional)", validUntilPicker),
                saveButton
        );
        leftCard.getStyleClass().add("event-card");
        leftCard.setPadding(new Insets(20));

        VBox rightCard = new VBox(
                14,
                sectionTitle("Live Preview"),
                styleLabel("Preview of the special ticket template", "card-text"),
                previewCard
        );
        rightCard.getStyleClass().add("event-card");
        rightCard.setPadding(new Insets(20));

        HBox content = new HBox(20, leftCard, rightCard);
        HBox.setHgrow(leftCard, Priority.ALWAYS);
        HBox.setHgrow(rightCard, Priority.ALWAYS);

        VBox existingSection = new VBox(14);
        existingSection.getChildren().addAll(
                sectionTitle("Existing Special Tickets"),
                styleLabel("Save the template above, then issue it here. Each issued ticket gets its own unique QR/barcode and SoldTickets row.", "card-text"),
                createExistingTicketsPane()
        );

        page.getChildren().addAll(topBar, subtitle, content, existingSection);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        return scrollPane;
    }

    private boolean handleSave(TextField ticketNameField,
                               TextArea descriptionArea,
                               TextField benefitField,
                               TextField priceField,
                               TextField quantityField,
                               boolean validForAllEvents,
                               ComboBox<String> eventComboBox,
                               DatePicker validUntilPicker) {
        String ticketName = ticketNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String benefit = benefitField.getText().trim();
        String selectedEventTitle = eventComboBox.getValue();

        if (ticketName.isBlank()) {
            AlertHelper.showError("Invalid Special Ticket", "Ticket name is required.");
            return false;
        }

        if (!validForAllEvents && (selectedEventTitle == null || selectedEventTitle.isBlank())) {
            AlertHelper.showError("Invalid Special Ticket", "Please select an event.");
            return false;
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
            return false;
        }

        try {
            String rawPrice = priceField.getText().trim().replace(",", ".");
            priceValue = Double.parseDouble(rawPrice);
            if (priceValue < 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            AlertHelper.showError("Invalid Special Ticket", "Price must be a number 0 or greater.");
            return false;
        }

        String databaseDescription = mergeDescriptionAndMeta(description, benefit, validUntilPicker.getValue());

        ticketController.createSpecialTicketDefinition(
                validForAllEvents ? null : selectedEventTitle,
                ticketName,
                databaseDescription,
                formatPrice(priceValue),
                quantity,
                validForAllEvents
        );

        AlertHelper.showInfo("Special Ticket Created", "The special ticket was saved successfully.");
        return true;
    }

    private FlowPane createExistingTicketsPane() {
        FlowPane cards = new FlowPane();
        cards.setHgap(16);
        cards.setVgap(16);
        cards.setPrefWrapLength(960);

        List<SpecialTicketRecord> records = ticketController.getSpecialTicketRecords();
        if (records.isEmpty()) {
            VBox emptyCard = new VBox(10);
            emptyCard.getStyleClass().add("event-card");
            emptyCard.setPadding(new Insets(18));
            emptyCard.setPrefWidth(320);

            Label emptyTitle = new Label("No special tickets yet");
            emptyTitle.getStyleClass().add("card-title");

            Label emptyText = new Label("Create a special ticket above and it will appear here.");
            emptyText.getStyleClass().add("card-text");
            emptyText.setWrapText(true);

            emptyCard.getChildren().addAll(emptyTitle, emptyText);
            cards.getChildren().add(emptyCard);
            return cards;
        }

        for (SpecialTicketRecord record : records) {
            cards.getChildren().add(createSpecialTicketCard(record));
        }

        return cards;
    }

    private VBox createSpecialTicketCard(SpecialTicketRecord record) {
        Label name = new Label(record.getSpecialTicketName());
        name.getStyleClass().add("card-title");
        name.setWrapText(true);

        Label status = new Label(record.isActive() ? "Ready to Generate" : "Already Issued");
        status.getStyleClass().add(record.isActive() ? "summary-value-highlight" : "card-text");

        HBox header = new HBox(12, name, grow(), status);
        header.setAlignment(Pos.CENTER_LEFT);

        Label description = new Label(safeText(record.getDescription(), "No description"));
        description.getStyleClass().add("card-text");
        description.setWrapText(true);

        Label price = new Label("Price: " + safeText(record.getPrice(), "Free"));
        price.getStyleClass().add("card-text");

        Label quantity = new Label("Quantity: " + record.getQuantity());
        quantity.getStyleClass().add("card-text");

        Label validFor = new Label("Valid For: " + (record.isValidForAllEvents()
                ? "All events"
                : safeText(record.getEventName(), "No event")));
        validFor.getStyleClass().add("card-text");

        Button generateButton = new Button("Generate Tickets");
        generateButton.getStyleClass().add("primary-btn");
        generateButton.setDisable(!record.isActive());
        generateButton.setOnAction(e -> issueSpecialTicket(record));

        Button removeButton = new Button(record.isActive() ? "Remove" : "Removed");
        removeButton.getStyleClass().add("danger-btn");
        removeButton.setDisable(!record.isActive());
        removeButton.setOnAction(e -> {
            boolean confirmed = AlertHelper.showConfirmation(
                    "Delete Special Ticket",
                    "Are you sure you want to remove this special ticket?"
            );
            if (!confirmed) {
                return;
            }

            boolean changed = ticketController.deactivateSpecialTicketGroup(record.getPublicCode());
            if (changed) {
                AlertHelper.showInfo("Special Ticket Removed", "The special ticket was removed successfully.");
                mainView.setContent(new CreateSpecialTicketView(mainView).getView());
            } else {
                AlertHelper.showError("Delete Failed", "The special ticket could not be removed.");
            }
        });

        HBox actions = new HBox(10, generateButton, removeButton);

        VBox card = new VBox(12, header, description, price, quantity, validFor, actions);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(18));
        card.setPrefWidth(340);
        return card;
    }

    private void issueSpecialTicket(SpecialTicketRecord record) {
        Event selectedEvent = null;

        if (!record.isValidForAllEvents()) {
            selectedEvent = findEventByTitle(record.getEventName());
            if (selectedEvent == null) {
                AlertHelper.showError("Generate Failed", "The event for this special ticket could not be found.");
                return;
            }
        }

        List<Ticket> generatedTickets = ticketController.issueSpecialTicketDefinition(record, selectedEvent);
        if (generatedTickets == null || generatedTickets.isEmpty()) {
            AlertHelper.showError("Generate Failed", "No special tickets were generated.");
            return;
        }

        String scope = record.isValidForAllEvents() ? "All events" : safeText(record.getEventName(), "Selected event");
        AlertHelper.showInfo(
                "Special Tickets Generated",
                generatedTickets.size() + " \"" + record.getSpecialTicketName() + "\" tickets were generated for " + scope + "."
        );
        mainView.showCoordinatorDashboard("Special Tickets");
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

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String formatPreviewPrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return "Free";
        }

        try {
            double value = Double.parseDouble(rawPrice.trim().replace(",", "."));
            return formatPrice(value);
        } catch (Exception ex) {
            return "Free";
        }
    }

    private String formatPrice(double amount) {
        if (amount == 0) {
            return "Free";
        }
        if (amount == Math.floor(amount)) {
            return String.format(Locale.ENGLISH, "%.0f DKK", amount);
        }
        return String.format(Locale.ENGLISH, "%.2f DKK", amount);
    }

    private String buildPreviewNotes(String description, LocalDate validUntil) {
        if (validUntil == null) {
            return description;
        }
        return description + " Valid until " + validUntil + ".";
    }

    private String mergeDescriptionAndMeta(String description, String benefit, LocalDate validUntil) {
        StringBuilder builder = new StringBuilder();

        if (description != null && !description.isBlank()) {
            builder.append(description.trim());
        }

        if (benefit != null && !benefit.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(" Benefit: ");
            } else {
                builder.append("Benefit: ");
            }
            builder.append(benefit.trim());
        }

        if (validUntil != null) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append("Valid until: ").append(validUntil);
        }

        return builder.toString();
    }

    private Event findEventByTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }

        for (Event event : eventController.getEvents()) {
            if (event.getTitle().equalsIgnoreCase(title.trim())) {
                return event;
            }
        }

        return null;
    }

    private Image bytesToImage(byte[] bytes) {
        return new Image(new ByteArrayInputStream(bytes));
    }
}
