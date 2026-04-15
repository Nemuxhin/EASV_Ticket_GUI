package easv.dal;

import easv.be.Customer;
import easv.be.SoldTicketRecord;
import easv.be.Ticket;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SoldTicketDAO {

    public void saveSoldTicket(Ticket ticket) {
        if (ticket == null) {
            return;
        }

        Customer customer = ticket.getCustomer();
        ensureTableExists();

        String sql = """
                INSERT INTO SoldTickets (EventName, CustomerName, CustomerEmail, TicketType, Price, IsUsed, PublicCode)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, blankToNull(ticket.getEventTitle()));
            statement.setString(2, customer != null ? customer.getName() : "");
            statement.setString(3, customer != null ? customer.getEmail() : "");
            statement.setString(4, ticket.getTicketType());
            statement.setBigDecimal(5, parsePrice(ticket.getPrice()));
            statement.setBoolean(6, ticket.isUsed());
            statement.setString(7, ticket.getSecureToken());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not save sold ticket.", ex);
        }
    }

    public List<SoldTicketRecord> getAllSoldTickets() {
        ensureTableExists();

        String sql = """
                SELECT SoldTicketID, EventName, CustomerName, CustomerEmail, TicketType, Price, IsUsed, PublicCode
                FROM SoldTickets
                ORDER BY SoldTicketID DESC
                """;

        List<SoldTicketRecord> records = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                records.add(new SoldTicketRecord(
                        resultSet.getInt("SoldTicketID"),
                        resultSet.getString("EventName"),
                        resultSet.getString("CustomerName"),
                        resultSet.getString("CustomerEmail"),
                        resultSet.getString("TicketType"),
                        formatPrice(resultSet.getBigDecimal("Price")),
                        resultSet.getBoolean("IsUsed"),
                        resultSet.getString("PublicCode")
                ));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load sold tickets.", ex);
        }

        return records;
    }

    public boolean setUsedState(String publicCode, boolean used) {
        ensureTableExists();

        String sql = """
                UPDATE SoldTickets
                SET IsUsed = ?
                WHERE PublicCode = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, used);
            statement.setString(2, publicCode);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not update sold ticket state.", ex);
        }
    }

    public boolean existsByPublicCode(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            return false;
        }

        ensureTableExists();

        String sql = """
                SELECT 1
                FROM SoldTickets
                WHERE PublicCode = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, publicCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not check sold ticket existence.", ex);
        }
    }

    private void ensureTableExists() {
        String sql = """
                IF OBJECT_ID(N'dbo.SoldTickets', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.SoldTickets (
                        SoldTicketID INT IDENTITY(1,1) PRIMARY KEY,
                        EventName NVARCHAR(255) NULL,
                        CustomerName NVARCHAR(255) NOT NULL,
                        CustomerEmail NVARCHAR(255) NOT NULL,
                        TicketType NVARCHAR(255) NOT NULL,
                        Price DECIMAL(10,2) NOT NULL,
                        IsUsed BIT NOT NULL CONSTRAINT DF_SoldTickets_IsUsed DEFAULT 0,
                        PublicCode NVARCHAR(255) NOT NULL UNIQUE
                    );
                END
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not create sold tickets table.", ex);
        }
    }

    private BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank() || "Free".equalsIgnoreCase(rawPrice.trim())) {
            return BigDecimal.ZERO;
        }

        String cleaned = rawPrice
                .replace("DKK", "")
                .replace("dkk", "")
                .replace(",", ".")
                .trim();

        return cleaned.isBlank() ? BigDecimal.ZERO : new BigDecimal(cleaned);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null || BigDecimal.ZERO.compareTo(price) == 0) {
            return "Free";
        }

        BigDecimal normalized = price.stripTrailingZeros();
        return normalized.toPlainString() + " DKK";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
