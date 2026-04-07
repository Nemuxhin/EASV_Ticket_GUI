package easv.dal;

import easv.be.Customer;
import easv.be.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TicketDAO {

    private final CustomerDAO customerDAO = new CustomerDAO();

    public void addTicket(Ticket ticket) {
        String sql = """
                INSERT INTO Tickets (PublicCode, TicketTypes, EventID, Price, IsUsed, DiscountType)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Customer savedCustomer = customerDAO.save(connection, ticket.getCustomer());
            Integer eventId = findEventId(connection, ticket.getEventTitle());
            statement.setString(1, ticket.getSecureToken());
            statement.setString(2, ticket.getTicketType());
            setNullableInt(statement, 3, eventId);
            statement.setBigDecimal(4, parsePrice(ticket.getPrice()));
            statement.setBoolean(5, ticket.isUsed());
            statement.setString(6, ticket.getTicketType());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next() && savedCustomer != null) {
                    addCustomerTicket(connection, keys.getInt(1), Integer.parseInt(savedCustomer.getCustomerId()));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not add ticket.", ex);
        }
    }

    public List<Ticket> getAllTickets() {
        String sql = """
                SELECT t.TicketID, t.PublicCode, t.TicketTypes, t.Price, t.IsUsed, t.DiscountType,
                       e.Name, e.Date, e.EndDate, e.Location, e.LocationGuidance, e.Notes,
                       c.CustomerID, c.CustomerName, c.CustomerEmail
                FROM Tickets t
                LEFT JOIN Events e ON e.EventID = t.EventID
                LEFT JOIN CustomerTickets ct ON ct.TicketID = t.TicketID
                LEFT JOIN Customers c ON c.CustomerID = ct.CustomerID
                """;

        List<Ticket> tickets = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                tickets.add(mapTicket(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load tickets.", ex);
        }

        return tickets;
    }

    public Ticket findByToken(String secureToken) {
        String sql = """
                SELECT TOP 1 t.TicketID, t.PublicCode, t.TicketTypes, t.Price, t.IsUsed, t.DiscountType,
                       e.Name, e.Date, e.EndDate, e.Location, e.LocationGuidance, e.Notes,
                       c.CustomerID, c.CustomerName, c.CustomerEmail
                FROM Tickets t
                LEFT JOIN Events e ON e.EventID = t.EventID
                LEFT JOIN CustomerTickets ct ON ct.TicketID = t.TicketID
                LEFT JOIN Customers c ON c.CustomerID = ct.CustomerID
                WHERE t.PublicCode = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, secureToken);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTicket(resultSet);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find ticket.", ex);
        }

        return null;
    }

    public boolean markTicketAsUsed(String secureToken) {
        String sql = "UPDATE Tickets SET IsUsed = 1 WHERE PublicCode = ? AND IsUsed = 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, secureToken);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not mark ticket as used.", ex);
        }
    }

    private Ticket mapTicket(ResultSet resultSet) throws SQLException {
        Customer customer = null;

        int customerId = resultSet.getInt("CustomerID");
        if (!resultSet.wasNull()) {
            customer = new Customer(
                    String.valueOf(customerId),
                    resultSet.getString("CustomerName"),
                    resultSet.getString("CustomerEmail")
            );
        }

        return new Ticket(
                String.valueOf(resultSet.getInt("TicketID")),
                resultSet.getString("PublicCode"),
                resultSet.getBoolean("IsUsed"),
                customer,
                resultSet.getString("Name"),
                String.valueOf(resultSet.getTimestamp("Date")),
                String.valueOf(resultSet.getTimestamp("EndDate")),
                resultSet.getString("Location"),
                resultSet.getString("LocationGuidance"),
                resultSet.getString("Notes"),
                resultSet.getString("TicketTypes"),
                resultSet.getString("DiscountType"),
                String.valueOf(resultSet.getBigDecimal("Price")),
                false,
                false,
                new byte[0],
                new byte[0]
        );
    }

    private Integer findEventId(Connection connection, String eventTitle) throws SQLException {
        if (eventTitle == null || eventTitle.isBlank()) {
            return null;
        }

        String sql = "SELECT TOP 1 EventID FROM Events WHERE Name = ? ORDER BY EventID DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventTitle);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("EventID");
                }
            }
        }

        return null;
    }

    private void addCustomerTicket(Connection connection, int ticketId, int customerId) throws SQLException {
        String sql = "INSERT INTO CustomerTickets (TicketID, CustomerID) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ticketId);
            statement.setInt(2, customerId);
            statement.executeUpdate();
        }
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.isBlank() || "Free".equalsIgnoreCase(priceText.trim())) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(priceText.replace("DKK", "").trim().replace(",", "."));
    }
}
