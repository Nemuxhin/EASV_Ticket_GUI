package Java.Gui;

import Java.gui.controller.EventController;
import Java.gui.controller.LoginController;
import Java.gui.controller.UserController;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MainView {

    private final StackPane rootPane;

    private final LoginController loginController;
    private final EventController eventController;
    private final UserController userController;

    public MainView() {
        rootPane = new StackPane();

        loginController = new LoginController();
        eventController = new EventController();
        userController = new UserController();

        showPortalSelection();
    }

    public Parent getRoot() {
        return rootPane;
    }

    public void showPortalSelection() {
        rootPane.getChildren().setAll(
                new PortalLoginView(this).getView()
        );
    }

    public void showLogin(String role) {
        rootPane.getChildren().setAll(
                new LoginView(this, loginController, role).getView()
        );
    }

    public void showAdminDashboard(String activeTab) {
        rootPane.getChildren().setAll(
                new AdminDashboardView(this, eventController, userController, activeTab).getView()
        );
    }

    public void showCoordinatorDashboard(String activeTab) {
        rootPane.getChildren().setAll(
                new CoordinatorDashboardView(this, eventController, userController, activeTab).getView()
        );
    }
}