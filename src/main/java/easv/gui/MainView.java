package easv.gui;

import easv.be.Event;
import easv.be.User;
import easv.controller.EventController;
import easv.controller.LoginController;
import easv.controller.UserController;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MainView {

    private final StackPane rootPane;
    private Event editingEvent;
    private User currentUser;

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

    public void setContent(Node node) {
        rootPane.getChildren().setAll(node);
    }

    public void showPortalSelection() {
        clearCurrentUser();
        setContent(new PortalLoginView(this).getView());
    }

    public void showLogin(String role) {
        setContent(new LoginView(this, loginController, role).getView());
    }

    public void showAdminDashboard(String activeTab) {
        setContent(new AdminDashboardView(this, eventController, userController, activeTab).getView());
    }

    public void showCoordinatorDashboard(String activeTab) {
        setContent(new CoordinatorDashboardView(this, eventController, userController, activeTab).getView());
    }

    public void showEditEvent(Event event) {
        editingEvent = event;
        showCoordinatorDashboard("Edit Event");
    }

    public Event getEditingEvent() {
        return editingEvent;
    }

    public void clearEditingEvent() {
        editingEvent = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void clearCurrentUser() {
        this.currentUser = null;
    }

    public void showTicketSales(Event selectedEvent) {
        setContent(new TicketSalesView(this, eventController, selectedEvent).getView());
    }

    public void showTicketScanner(Event selectedEvent) {
        setContent(new TicketScanView(this, selectedEvent).getView());
    }
}
