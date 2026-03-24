package Java.bll;

import Java.be.User;
import Java.dal.LoginDAO;
import Java.dal.UserDAO;

import java.util.List;

public class UserManager {

    private final UserDAO userDAO;
    private final LoginDAO loginDAO;

    public UserManager() {
        this.userDAO = new UserDAO();
        this.loginDAO = new LoginDAO();
    }

    public boolean login(String username, String password, String role) {
        return loginDAO.validateLogin(username, password, role);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public List<User> getUsersByRole(String role) {
        return userDAO.getUsersByRole(role);
    }

    public void createUser(User user) {
        userDAO.addUser(user);
    }

    public void deleteUser(User user) {
        userDAO.deleteUser(user);
    }
}