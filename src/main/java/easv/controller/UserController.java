package easv.controller;

import easv.be.User;
import easv.bll.UserManager;

import java.util.List;

public class UserController {

    private final UserManager userManager = new UserManager();

    public List<User> getUsersByRole(String role) {
        return userManager.getUsersByRole(role);
    }

    public void createUser(User user) {
        userManager.createUser(user);
    }

    public void deleteUser(User user) {
        userManager.deleteUser(user);
    }
}