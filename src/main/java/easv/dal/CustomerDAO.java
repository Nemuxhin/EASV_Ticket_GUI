package easv.dal;

import easv.be.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerDAO {

    public Customer findById(int customerId) {
        String sql = "SELECT CustomerID, CustomerName, CustomerEmail FROM Customers WHERE CustomerID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCustomer(resultSet);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find customer.", ex);
        }

        return null;
    }

    public Customer findByEmail(String email) {
        String sql = "SELECT CustomerID, CustomerName, CustomerEmail FROM Customers WHERE CustomerEmail = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCustomer(resultSet);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find customer.", ex);
        }

        return null;
    }

    public Customer save(Customer customer) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return save(connection, customer);
        } catch (SQLException ex) {
            throw new RuntimeException("Could not save customer.", ex);
        }
    }

    Customer save(Connection connection, Customer customer) throws SQLException {
        if (customer == null) {
            return null;
        }

        Customer existing = findByEmail(connection, customer.getEmail());
        if (existing != null) {
            return existing;
        }

        String sql = "INSERT INTO Customers (CustomerName, CustomerEmail) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, customer.getName());
            statement.setString(2, customer.getEmail());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Customer(String.valueOf(keys.getInt(1)), customer.getName(), customer.getEmail());
                }
            }
        }

        return customer;
    }

    private Customer findByEmail(Connection connection, String email) throws SQLException {
        String sql = "SELECT CustomerID, CustomerName, CustomerEmail FROM Customers WHERE CustomerEmail = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCustomer(resultSet);
                }
            }
        }

        return null;
    }

    private Customer mapCustomer(ResultSet resultSet) throws SQLException {
        return new Customer(
                String.valueOf(resultSet.getInt("CustomerID")),
                resultSet.getString("CustomerName"),
                resultSet.getString("CustomerEmail")
        );
    }
}
