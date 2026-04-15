package easv.gui;

import easv.controller.LoginController;
import easv.be.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

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

        VBox page = new VBox();
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(36));

        VBox shell = new VBox(14);
        shell.setAlignment(Pos.CENTER);
        shell.setMaxWidth(390);
        shell.setPrefWidth(390);
        shell.getStyleClass().add("portal-login-shell");

        StackPane iconCircle = createPortalIcon();

        Label titleLbl = new Label(getPortalTitle());
        titleLbl.getStyleClass().add("portal-login-title");

        Label subtitleLbl = new Label("Sign in to access your dashboard");
        subtitleLbl.getStyleClass().add("portal-login-subtitle");

        VBox formBox = new VBox(12);
        formBox.setFillWidth(true);
        formBox.setMaxWidth(Double.MAX_VALUE);
        formBox.setPadding(new Insets(8, 0, 0, 0));

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("form-label");

        TextField userField = new TextField();
        userField.setPromptText(getUsernamePlaceholder());
        userField.setMaxWidth(Double.MAX_VALUE);
        userField.getStyleClass().addAll("input-field", "portal-input");

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("form-label");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setMaxWidth(Double.MAX_VALUE);
        passField.getStyleClass().addAll("input-field", "portal-input");

        Button loginBtn = new Button("Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.getStyleClass().addAll("primary-btn", "portal-primary-btn");
        loginBtn.setGraphic(createEnterIcon(0.92, Color.WHITE));
        loginBtn.setGraphicTextGap(8);
        loginBtn.setContentDisplay(ContentDisplay.LEFT);
        loginBtn.setAlignment(Pos.CENTER);

        Button backBtn = new Button("Back to Portal Selection");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.getStyleClass().addAll("secondary-btn", "portal-secondary-btn");
        backBtn.setGraphic(createBackIcon(0.92, Color.web("#334155")));
        backBtn.setGraphicTextGap(8);
        backBtn.setContentDisplay(ContentDisplay.LEFT);
        backBtn.setAlignment(Pos.CENTER);
        backBtn.setOnAction(e -> mainView.showPortalSelection());

        Runnable doLogin = () -> {
            User user = loginController.authenticate(
                    userField.getText().trim(),
                    passField.getText(),
                    role
            );

            if (user == null) {
                AlertHelper.showError("Login Failed", "The username, password, or role is incorrect.");
                return;
            }

            mainView.setCurrentUser(user);

            if ("Admin".equals(role)) {
                mainView.showAdminDashboard("Coordinators");
            } else {
                mainView.showCoordinatorDashboard("Events");
            }
        };


        loginBtn.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());

        formBox.getChildren().addAll(
                userLbl, userField,
                passLbl, passField,
                loginBtn,
                backBtn
        );

        shell.getChildren().addAll(
                iconCircle,
                titleLbl,
                subtitleLbl,
                formBox
        );

        page.getChildren().add(shell);
        root.setCenter(page);

        return root;
    }

    private String getPortalTitle() {
        return "Admin".equals(role) ? "Admin Portal" : "Event Coordinator Portal";
    }

    private String getUsernamePlaceholder() {
        return "Admin".equals(role) ? "admin" : "coordinator01";
    }


    private StackPane createPortalIcon() {
        StackPane circle = new StackPane();
        circle.getStyleClass().add("portal-login-icon-circle");
        circle.getChildren().add(createEnterIcon(1.9, Color.WHITE));
        return circle;
    }

    private Group createEnterIcon(double scale, Color color) {
        Line arrowLine = new Line(4, 10, 15, 10);

        Polyline arrowHead = new Polyline(
                11, 6,
                15, 10,
                11, 14
        );

        Line frameRight = new Line(20, 5, 20, 15);
        Line frameTop = new Line(13, 5, 20, 5);
        Line frameBottom = new Line(13, 15, 20, 15);

        Shape[] parts = {arrowLine, arrowHead, frameRight, frameTop, frameBottom};

        for (Shape part : parts) {
            part.setStroke(color);
            part.setStrokeWidth(2.2);
            part.setStrokeLineCap(StrokeLineCap.ROUND);
            part.setStrokeLineJoin(StrokeLineJoin.ROUND);
            part.setFill(Color.TRANSPARENT);
        }

        Group group = new Group(arrowLine, arrowHead, frameRight, frameTop, frameBottom);
        group.setScaleX(scale);
        group.setScaleY(scale);
        return group;
    }

    private Group createBackIcon(double scale, Color color) {
        Line arrowLine = new Line(15, 10, 5, 10);

        Polyline arrowHead = new Polyline(
                9, 6,
                5, 10,
                9, 14
        );

        Shape[] parts = {arrowLine, arrowHead};

        for (Shape part : parts) {
            part.setStroke(color);
            part.setStrokeWidth(2.2);
            part.setStrokeLineCap(StrokeLineCap.ROUND);
            part.setStrokeLineJoin(StrokeLineJoin.ROUND);
            part.setFill(Color.TRANSPARENT);
        }

        Group group = new Group(arrowLine, arrowHead);
        group.setScaleX(scale);
        group.setScaleY(scale);
        return group;
    }
}