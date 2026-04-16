package easv.dal;

import easv.be.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<User> getAllUsers() {
        String sql = "SELECT Names, Username, Password, UserEmail, Role FROM Users ORDER BY Role, Names";
        return queryUsers(sql);
    }

    public List<User> getUsersByRole(String role) {
        String sql = "SELECT Names, Username, Password, UserEmail, Role FROM Users WHERE Role = ? ORDER BY Names";
        return queryUsers(sql, role);
    }

    public void addUser(User user) {
        String sql = "INSERT INTO Users (Names, Username, Password, UserEmail, Role) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getRole());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not add user.", ex);
        }
    }

    public void deleteUser(User user) {
        String userIdSql = "SELECT TOP 1 UserID FROM Users WHERE Username = ? AND Role = ?";
        String deleteAssignmentsSql = "DELETE FROM UserEvent WHERE UserID = ?";
        String deleteUserSql = "DELETE FROM Users WHERE Username = ? AND Role = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Integer userId = null;

                try (PreparedStatement userIdStatement = connection.prepareStatement(userIdSql)) {
                    userIdStatement.setString(1, user.getUsername());
                    userIdStatement.setString(2, user.getRole());

                    try (ResultSet resultSet = userIdStatement.executeQuery()) {
                        if (resultSet.next()) {
                            userId = resultSet.getInt("UserID");
                        }
                    }
                }

                if (userId != null) {
                    try (PreparedStatement assignmentStatement = connection.prepareStatement(deleteAssignmentsSql)) {
                        assignmentStatement.setInt(1, userId);
                        assignmentStatement.executeUpdate();
                    }
                }

                try (PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserSql)) {
                    deleteUserStatement.setString(1, user.getUsername());
                    deleteUserStatement.setString(2, user.getRole());
                    deleteUserStatement.executeUpdate();
                }

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not delete user.", ex);
        }
    }

    public void updateUser(User user, String previousUsername, String previousRole) {
        String sql = """
                UPDATE Users
                SET Names = ?, Username = ?, Password = ?, UserEmail = ?, Role = ?
                WHERE Username = ? AND Role = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getRole());
            statement.setString(6, previousUsername);
            statement.setString(7, previousRole);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not update user.", ex);
        }
    }

    public boolean usernameExists(String username, String excludedUsername) {
        if (username == null || username.isBlank()) {
            return false;
        }

        String sql;
        if (excludedUsername == null || excludedUsername.isBlank()) {
            sql = "SELECT 1 FROM Users WHERE Username = ?";
        } else {
            sql = "SELECT 1 FROM Users WHERE Username = ? AND Username <> ?";
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());
            if (excludedUsername != null && !excludedUsername.isBlank()) {
                statement.setString(2, excludedUsername.trim());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not validate username uniqueness.", ex);
        }
    }

    public User findUser(String username, String password, String role) {
        String sql = "SELECT Names, Username, Password, UserEmail, Role FROM Users WHERE Username = ? AND Password = ? AND Role = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find user.", ex);
        }

        return null;
    }

    public boolean validateLogin(String username, String password, String role) {
        return findUser(username, password, role) != null;
    }

    private List<User> queryUsers(String sql, Object... parameters) {
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load users.", ex);
        }

        return users;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getString("Names"),
                resultSet.getString("Username"),
                resultSet.getString("Password"),
                resultSet.getString("UserEmail"),
                resultSet.getString("Role")
        );
    }
}
