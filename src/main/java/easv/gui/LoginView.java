package easv.gui;

import easv.controller.LoginController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class LoginView {

    private final MainView mainView;
    private final LoginController loginController;
    private final String role;

    public LoginView(MainView mainView, LoginController loginController, String role) {
        this.mainView = mainView;
        this.loginController = loginController;
        this.role = role;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-bg");

        VBox page = new VBox(30);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(40));

        VBox header = createHeader(
                "EASV Tickets - " + role,
                "",
                "Login",
                "Sign in to continue"
        );

        VBox formBox = new VBox(15);
        formBox.setMaxWidth(360);
        formBox.getStyleClass().add("login-form");
        formBox.setPadding(new Insets(30));

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("form-label");

        TextField userField = new TextField();
        userField.getStyleClass().add("input-field");

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("form-label");

        PasswordField passField = new PasswordField();
        passField.getStyleClass().add("input-field");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("primary-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> mainView.showPortalSelection());

        // Temporary: no real login yet, just open the selected dashboard.
        loginBtn.setOnAction(e -> {
            if ("Admin".equals(role)) {
                mainView.showAdminDashboard("Coordinators");
            } else {
                mainView.showCoordinatorDashboard("Events");
            }
        });

        formBox.getChildren().addAll(
                userLbl, userField,
                passLbl, passField,
                loginBtn,
                backBtn
        );

        page.getChildren().addAll(header, formBox);
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
}