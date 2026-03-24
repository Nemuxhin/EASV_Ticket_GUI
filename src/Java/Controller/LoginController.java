package Java.gui.controller;

import Java.Bll.UserManager;

public class LoginController {

    private final UserManager userManager = new UserManager();

    public boolean isValid(String username, String password, String role) {
        return userManager.login(username, password, role);
    }
}