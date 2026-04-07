package easv.bll;

import easv.be.User;
import easv.dal.UserDAO;

import java.util.List;
import java.util.regex.Pattern;

public class UserManager {

    private final UserDAO userDAO = new UserDAO();

    public boolean login(String username, String password, String role) {
        return userDAO.findUser(username, password, role) != null;
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

    public String validateCoordinatorInput(String name, String email, String username, String password) {
        if (name == null || name.isBlank() ||
                email == null || email.isBlank() ||
                username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            return "Please fill in all coordinator fields.";
        }

        if (!isValidEmail(email)) {
            return "Please enter a valid email address.";
        }

        return null;
    }

    public boolean isValidEmail(String email) {
        return email != null &&
                Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email);
    }

    public void updateUser(User user, String name, String email, String username, String password) {
        user.setName(name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);

        userDAO.updateUser(user);
    }
}