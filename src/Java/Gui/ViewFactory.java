package Java.Gui;

import Java.Be.Event;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ViewFactory {


    public static VBox createHeader(String appTitle, String appSubtitle, String pageTitle, String pageSubtitle) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Label l1 = new Label("SEA"); l1.getStyleClass().add("logo-large");
        Label l2 = new Label("Erhvervsakademi"); l2.getStyleClass().add("logo-small");
        Label l3 = new Label(appTitle); l3.getStyleClass().add("app-title");

        box.getChildren().addAll(l1, l2, l3);

        if (!appSubtitle.isEmpty()) {
            Label l4 = new Label(appSubtitle); l4.getStyleClass().add("app-subtitle");
            box.getChildren().add(l4);
        }

        if (!pageTitle.isEmpty()) {
            VBox spacing = new VBox(); spacing.setMinHeight(20);
            Label pt = new Label(pageTitle); pt.getStyleClass().add("page-title-center");
            Label ps = new Label(pageSubtitle); ps.getStyleClass().add("page-subtitle-center");
            box.getChildren().addAll(spacing, pt, ps);
        }
        return box;
    }

    /**
     * Creates the "Portal Cards" (Admin, Coordinator, Customer) with icons.
     */
    public static VBox createPortalCard(String icon, String title, String subtitle) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("portal-card");

        Label ic = new Label(icon); ic.getStyleClass().add("portal-icon");
        Label tL = new Label(title); tL.getStyleClass().add("card-title");
        Label sL = new Label(subtitle); sL.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(ic, tL, sL);
        return card;
    }

    /**
     * Builds the detailed event cards used in the dashboards.
     */
    public static VBox buildDynamicCard(Event ev, String viewMode, Runnable onAction) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");

        HBox top = new HBox();
        Label tLbl = new Label(ev.getTitle()); tLbl.getStyleClass().add("card-title");
        Label sLbl = new Label(ev.getStatus());
        sLbl.getStyleClass().add(ev.getStatus().equals("Available") ? "status-avail" : "status-fast");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(tLbl, spacer, sLbl);

        card.getChildren().addAll(top,
                new Label("🕒 " + ev.getDate()){{getStyleClass().add("card-text");}},
                new Label("📍 " + ev.getLocation()){{getStyleClass().add("card-text");}},
                new Label("Notes"){{getStyleClass().add("notes-head");}},
                new Label(ev.getNotes()){{getStyleClass().add("card-text");}},
                new Separator());

        // CUSTOMER VIEW: Show Price and Buy Button
        if (viewMode.equals("CUSTOMER")) {
            Button buyBtn = new Button("Buy Ticket");
            buyBtn.getStyleClass().add("primary-btn");
            buyBtn.setMaxWidth(Double.MAX_VALUE);
            buyBtn.setOnAction(e -> onAction.run());

            card.getChildren().addAll(new Label(ev.getPrice()){{getStyleClass().add("price-text");}}, buyBtn);
        }
        // COORDINATOR VIEW: Show assigned pills and Assign Button
        else if (viewMode.equals("COORD_ACCESS")) {
            FlowPane pillBox = new FlowPane(5, 5);
            for (String c : ev.getCoordinators()) {
                Label pill = new Label(c); pill.getStyleClass().add("coord-pill");
                pillBox.getChildren().add(pill);
            }
            Button assignBtn = new Button("👥 Assign Access"); assignBtn.getStyleClass().add("primary-btn");
            assignBtn.setMaxWidth(Double.MAX_VALUE);

            card.getChildren().addAll(new Label("Assigned Coordinators"){{getStyleClass().add("notes-head");}}, pillBox, assignBtn);
        }
        // ADMIN/OTHER: Show Delete button
        else {
            Button delBtn = new Button("🗑 Delete Event");
            delBtn.getStyleClass().add("danger-btn");
            delBtn.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(delBtn);
        }

        return card;
    }

    /**
     * Creates buttons for the dashboard sidebars.
     */
    public static Button createMenuBtn(String text, boolean isActive, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        btn.getStyleClass().add(isActive ? "sidebar-menu-btn-active" : "sidebar-menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(action);
        return btn;
    }
}