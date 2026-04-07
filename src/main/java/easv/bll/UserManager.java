package easv.bll;

import easv.be.User;
import easv.dal.UserDAO;

import java.util.List;

public class UserManager {

    private final UserDAO userDAO = new UserDAO();

    public boolean login(String username, String password, String role) {
        return userDAO.validateLogin(username, password, role);
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
