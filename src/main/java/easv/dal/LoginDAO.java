package easv.dal;

import easv.be.User;

public class LoginDAO {

    private final UserDAO userDAO = new UserDAO();

    public boolean validateLogin(String username, String password, String role) {
        User user = userDAO.findUser(username, password, role);
        return user != null;
    }
}