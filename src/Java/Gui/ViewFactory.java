package Java.Gui;

import Java.Be.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class ViewFactory {
    public static VBox createHeader(String appTitle, String appSubtitle, String pageTitle, String pageSubtitle) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Label l1 = new Label("SEA");
        l1.getStyleClass().add("logo-large");
        Label l2 = new Label("Erhvervsakademi");
        l2.getStyleClass().add("logo-small");
        Label l3 = new Label(appTitle);
        l3.getStyleClass().add("app-title");

        box.getChildren().addAll(l1, l2, l3);

        if (!appSubtitle.isEmpty()) {
            Label l4 = new Label(appSubtitle);
            l4.getStyleClass().add("app-subtitle");
            box.getChildren().add(l4);
        }

        if (!pageTitle.isEmpty()) {
            VBox spacing = new VBox();
            spacing.setMinHeight(20);

            Label pt = new Label(pageTitle);
            pt.getStyleClass().add("page-title-center");
            Label ps = new Label(pageSubtitle);
            ps.getStyleClass().add("page-subtitle-center");

            box.getChildren().addAll(spacing, pt, ps);
        }

        return box;
    }

    public static VBox createPortalCard(String icon, String title, String subtitle) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("portal-card");

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("portal-icon");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);
        return card;
    }

    // Base card: common event info is built once and reused in every screen.
    public static VBox createEventCardBase(Event event) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("card-title");
        card.getChildren().add(titleLabel);
        card.getChildren().add(createCardText("Start date/time: " + event.getStartDateTime()));

        // SAMU: Optional values are shown only when they are filled in.
        if (event.hasEndDateTime()) {
            card.getChildren().add(createCardText("End date/time: " + event.getEndDateTime()));
        }

        card.getChildren().add(createCardText("Location: " + event.getLocation()));

        if (event.hasLocationGuidance()) {
            card.getChildren().add(createCardText("Location guidance: " + event.getLocationGuidance()));
        }

        Label notesHead = new Label("Notes");
        notesHead.getStyleClass().add("notes-head");
        Label notesLabel = createCardText(event.getNotes());

        card.getChildren().addAll(notesHead, notesLabel, new Separator());
        return card;
    }

    public static Button createMenuBtn(String text, boolean isActive, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        btn.getStyleClass().add(isActive ? "sidebar-menu-btn-active" : "sidebar-menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(action);
        return btn;
    }

    public static Label createPriceLabel(String price) {
        Label priceLabel = new Label(price);
        priceLabel.getStyleClass().add("price-text");
        return priceLabel;
    }

    public static FlowPane createCoordinatorPillBox(String[] coordinators) {
        FlowPane pillBox = new FlowPane(5, 5);

        if (coordinators.length == 0) {
            Label emptyLabel = createCardText("No coordinators assigned");
            pillBox.getChildren().add(emptyLabel);
            return pillBox;
        }

        for (String coordinator : coordinators) {
            Label pill = new Label(coordinator);
            pill.getStyleClass().add("coord-pill");
            pillBox.getChildren().add(pill);
        }

        return pillBox;
    }

    private static Label createCardText(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-text");
        return label;
    }
}
