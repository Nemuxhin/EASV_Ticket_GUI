package Java.controller;

import Java.bll.UserManager;

public class LoginController {

    private final UserManager userManager;

    public LoginController() {
        this.userManager = new UserManager();
    }

    public boolean login(String username, String password, String role) {
        return userManager.login(username, password, role);
    }
}