package easv.controller;

import easv.be.User;
import easv.bll.UserManager;

import java.util.List;

public class UserController {

    private final UserManager userManager = new UserManager();

    public List<User> getAllUsers() {
        return userManager.getAllUsers();
    }

    public List<User> getUsersByRole(String role) {
        return userManager.getUsersByRole(role);
    }

    public void createUser(User user) {
        userManager.createUser(user);
    }

    public void deleteUser(User user) {
        userManager.deleteUser(user);
    }

    public String validateUserInput(String name, String email, String username, String password, String role) {
        return userManager.validateUserInput(name, email, username, password, role);
    }

    public String validateUserUpdateInput(String name, String email, String username, String password, String role, String currentUsername) {
        return userManager.validateUserUpdateInput(name, email, username, password, role, currentUsername);
    }

    public String validateCoordinatorInput(String name, String email, String username, String password) {
        return userManager.validateCoordinatorInput(name, email, username, password);
    }

    public boolean isValidEmail(String email) {
        return userManager.isValidEmail(email);
    }

    public void updateUser(User user, String name, String email, String username, String password, String role) {
        userManager.updateUser(user, name, email, username, password, role);
    }
}
