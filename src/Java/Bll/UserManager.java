package Java.Bll;

import Java.Be.User;
import Java.Dal.LoginDAO;
import Java.Dal.UserDAO;

import java.util.List;

public class UserManager {

    private final UserDAO userDAO = new UserDAO();
    private final LoginDAO loginDAO = new LoginDAO();

    public boolean login(String username, String password, String role) {
        return loginDAO.validateLogin(username, password, role);
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