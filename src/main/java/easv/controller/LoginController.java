package easv.controller;

import easv.be.User;
import easv.bll.UserManager;

public class LoginController {

    private final UserManager userManager = new UserManager();

    public boolean isValid(String username, String password, String role) {
        return userManager.login(username, password, role);
    }

    public User authenticate(String username, String password, String role) {
        return userManager.authenticate(username, password, role);
    }
}
