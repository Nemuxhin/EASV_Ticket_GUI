package Java.dal;

public class LoginDAO {

    public boolean validateLogin(String username, String password, String role) {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}