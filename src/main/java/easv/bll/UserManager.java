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

    public String validateUserInput(String name, String email, String username, String password, String role) {
        return validateUserInputInternal(name, email, username, password, role, null);
    }

    public String validateUserUpdateInput(String name, String email, String username, String password, String role, String currentUsername) {
        return validateUserInputInternal(name, email, username, password, role, currentUsername);
    }

    public String validateCoordinatorInput(String name, String email, String username, String password) {
        return validateUserInputInternal(name, email, username, password, "Event Coordinator", null);
    }

    private String validateUserInputInternal(String name,
                                             String email,
                                             String username,
                                             String password,
                                             String role,
                                             String currentUsername) {
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {
            return "Please fill in all user fields.";
        }

        if (!isValidEmail(email)) {
            return "Please enter a valid email address.";
        }

        if (!isSupportedRole(role)) {
            return "Please choose either Admin or Event Coordinator.";
        }

        if (userDAO.usernameExists(username.trim(), currentUsername == null ? null : currentUsername.trim())) {
            return "That username is already in use.";
        }

        return null;
    }

    public boolean isValidEmail(String email) {
        return email != null
                && Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email);
    }

    public void updateUser(User user, String name, String email, String username, String password, String role) {
        String previousUsername = user.getUsername();
        String previousRole = user.getRole();

        user.setName(name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        userDAO.updateUser(user, previousUsername, previousRole);
    }

    public User authenticate(String username, String password, String role) {
        return userDAO.findUser(username, password, role);
    }

    private boolean isSupportedRole(String role) {
        return "Admin".equalsIgnoreCase(role) || "Event Coordinator".equalsIgnoreCase(role);
    }
}
