package Java.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PortalLoginView {

    private final MainView mainView;

    public PortalLoginView(MainView mainView) {
        this.mainView = mainView;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-bg");

        VBox page = new VBox(30);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(40));

        VBox header = createHeader(
                "EASV Tickets",
                "Event Ticket Management System",
                "Select Portal",
                "Choose your access level"
        );

        HBox cardsBox = new HBox(20);
        cardsBox.setAlignment(Pos.CENTER);

        VBox adminCard = createPortalCard("👤", "Admin", "Manage coordinators and events");
        adminCard.setOnMouseClicked(e -> mainView.showLogin("Admin"));

        VBox coordinatorCard = createPortalCard("📅", "Event Coordinator", "Manage events and access");
        coordinatorCard.setOnMouseClicked(e -> mainView.showLogin("Event Coordinator"));

        cardsBox.getChildren().addAll(adminCard, coordinatorCard);

        page.getChildren().addAll(header, cardsBox);
        root.setCenter(page);

        return root;
    }

    private VBox createHeader(String appTitle, String appSubtitle, String pageTitle, String pageSubtitle) {
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

    private VBox createPortalCard(String icon, String title, String subtitle) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("portal-card");

        Label ic = new Label(icon);
        ic.getStyleClass().add("portal-icon");

        Label tL = new Label(title);
        tL.getStyleClass().add("card-title");

        Label sL = new Label(subtitle);
        sL.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(ic, tL, sL);
        return card;
    }
}