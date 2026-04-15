package easv.dal;

import easv.be.Event;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.util.Locale;

public class EventDAO {
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter FORM_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm", Locale.ENGLISH);

    public List<Event> getAllEvents() {
        String sql = """
                SELECT EventID, Name, Location, Date, Notes, Status, EndDate, LocationGuidance, StandardPrice, Capacity
                FROM Events
                ORDER BY Date
                """;

        return getEventsByQuery(sql, "Could not load events.");
    }

    public List<Event> getArchivedEvents() {
        String sql = """
                SELECT EventID, Name, Location, Date, Notes, Status, EndDate, LocationGuidance, StandardPrice, Capacity
                FROM Events
                WHERE Status = 'Archived'
                ORDER BY Date DESC
                """;

        return getEventsByQuery(sql, "Could not load archived events.");
    }

    public void restoreEvent(Event event) {
        Integer eventId = findEventId(event);
        if (eventId == null) {
            return;
        }

        String sql = """
                UPDATE Events
                SET Status = 'Available'
                WHERE EventID = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not restore event.", ex);
        }
    }

    public void addEvent(Event event) {
        String sql = """
                INSERT INTO Events (Name, Location, Date, Notes, Status, EndDate, LocationGuidance, StandardPrice, Capacity)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setEventStatementValues(statement, event);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    setCoordinators(connection, keys.getInt(1), event.getCoordinators());
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not add event.", ex);
        }
    }

    public void deleteEvent(Event event) {
        Integer eventId = findEventId(event);
        if (eventId == null) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Events WHERE EventID = ?")) {
                deleteDependentRows(connection, event, eventId);

                statement.setInt(1, eventId);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not delete event.", ex);
        }
    }

    public boolean updateEvent(Event currentEvent, Event updatedEvent) {
        Integer eventId = findEventId(currentEvent);
        if (eventId == null) {
            return false;
        }

        String sql = """
                UPDATE Events
                SET Name = ?, Location = ?, Date = ?, Notes = ?, Status = ?, EndDate = ?, LocationGuidance = ?, StandardPrice = ?, Capacity = ?
                WHERE EventID = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setEventStatementValues(statement, updatedEvent);
            statement.setInt(10, eventId);
            int rowsUpdated = statement.executeUpdate();
            setCoordinators(connection, eventId, updatedEvent.getCoordinators());
            return rowsUpdated > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not update event.", ex);
        }
    }

    public void assignCoordinator(Event event, String coordinatorName) {
        Integer eventId = findEventId(event);
        Integer userId = findUserIdByName(coordinatorName);

        if (eventId == null || userId == null) {
            return;
        }

        String sql = """
                IF NOT EXISTS (SELECT 1 FROM UserEvent WHERE UserID = ? AND EventID = ?)
                INSERT INTO UserEvent (UserID, EventID) VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, eventId);
            statement.setInt(3, userId);
            statement.setInt(4, eventId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not assign coordinator.", ex);
        }
    }

    public void setCoordinators(Event event, String[] coordinators) {
        Integer eventId = findEventId(event);
        if (eventId == null) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            setCoordinators(connection, eventId, coordinators);
        } catch (SQLException ex) {
            throw new RuntimeException("Could not set coordinators.", ex);
        }
    }

    public void updateEventStatus(Event event, String status) {
        Integer eventId = findEventId(event);
        if (eventId == null) {
            return;
        }

        String sql = """
                UPDATE Events
                SET Status = ?
                WHERE EventID = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, eventId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not update event status.", ex);
        }
    }

    private Event mapEvent(ResultSet resultSet, String[] coordinators) throws SQLException {
        return new Event(
                resultSet.getString("Name"),
                formatDateTime(resultSet.getTimestamp("Date")),
                formatDateTime(resultSet.getTimestamp("EndDate")),
                resultSet.getString("Location"),
                resultSet.getString("LocationGuidance"),
                resultSet.getString("Notes"),
                formatPrice(resultSet.getBigDecimal("StandardPrice")),
                formatCapacity(resultSet),
                resultSet.getString("Status"),
                coordinators
        );
    }

    private List<Event> getEventsByQuery(String sql, String errorMessage) {
        List<Event> events = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int eventId = resultSet.getInt("EventID");
                events.add(mapEvent(resultSet, getCoordinators(connection, eventId)));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(errorMessage, ex);
        }

        return events;
    }

    private String[] getCoordinators(Connection connection, int eventId) throws SQLException {
        String sql = """
                SELECT u.Names
                FROM UserEvent ue
                JOIN Users u ON u.UserID = ue.UserID
                WHERE ue.EventID = ?
                ORDER BY u.Names
                """;

        List<String> coordinators = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    coordinators.add(resultSet.getString("Names"));
                }
            }
        }

        return coordinators.toArray(new String[0]);
    }

    private void setCoordinators(Connection connection, int eventId, String[] coordinators) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM UserEvent WHERE EventID = ?")) {
            deleteStatement.setInt(1, eventId);
            deleteStatement.executeUpdate();
        }

        if (coordinators == null || coordinators.length == 0) {
            return;
        }

        String insertSql = "INSERT INTO UserEvent (UserID, EventID) VALUES (?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            for (String coordinator : Arrays.copyOf(coordinators, coordinators.length)) {
                Integer userId = findUserIdByName(connection, coordinator);
                if (userId == null) {
                    continue;
                }

                insertStatement.setInt(1, userId);
                insertStatement.setInt(2, eventId);
                insertStatement.addBatch();
            }

            insertStatement.executeBatch();
        }
    }

    private void setEventStatementValues(PreparedStatement statement, Event event) throws SQLException {
        statement.setString(1, event.getTitle());
        statement.setString(2, event.getLocation());
        statement.setTimestamp(3, parseDateTime(event.getStartDateTime()));
        statement.setString(4, event.getNotes());
        statement.setString(5, event.getStatus());
        statement.setTimestamp(6, parseDateTime(event.getEndDateTime()));
        statement.setString(7, event.getLocationGuidance());
        statement.setBigDecimal(8, parsePrice(event.getPrice()));
        setNullableInt(statement, 9, event.getCapacity());
    }

    private Integer findEventId(Event event) {
        String sql = """
                SELECT TOP 1 EventID
                FROM Events
                WHERE Name = ? AND Location = ? AND Date = ?
                ORDER BY EventID DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, event.getTitle());
            statement.setString(2, event.getLocation());
            statement.setTimestamp(3, parseDateTime(event.getStartDateTime()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("EventID");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find event.", ex);
        }

        return null;
    }

    private Integer findUserIdByName(String name) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return findUserIdByName(connection, name);
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find user.", ex);
        }
    }

    private Integer findUserIdByName(Connection connection, String name) throws SQLException {
        String sql = "SELECT TOP 1 UserID FROM Users WHERE Names = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("UserID");
                }
            }
        }

        return null;
    }

    private Timestamp parseDateTime(String dateTimeText) {
        if (dateTimeText == null || dateTimeText.isBlank()) {
            return null;
        }

        for (DateTimeFormatter formatter : new DateTimeFormatter[]{DISPLAY_DATE_TIME, FORM_DATE_TIME}) {
            try {
                return Timestamp.valueOf(LocalDateTime.parse(dateTimeText.trim(), formatter));
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Invalid date/time value: " + dateTimeText);
    }

    private String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        return timestamp.toLocalDateTime().format(DISPLAY_DATE_TIME);
    }

    private BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.isBlank() || "Free".equalsIgnoreCase(priceText.trim())) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(priceText.replace("DKK", "").trim().replace(",", "."));
    }

    private String formatPrice(BigDecimal price) {
        if (price == null || BigDecimal.ZERO.compareTo(price) == 0) {
            return "Free";
        }

        BigDecimal normalized = price.stripTrailingZeros();
        return normalized.toPlainString() + " DKK";
    }

    private String formatCapacity(ResultSet resultSet) throws SQLException {
        int capacity = resultSet.getInt("Capacity");
        return resultSet.wasNull() ? "" : String.valueOf(capacity);
    }

    private void setNullableInt(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, java.sql.Types.INTEGER);
            return;
        }

        statement.setInt(index, Integer.parseInt(value.trim()));
    }

    private void deleteDependentRows(Connection connection, Event event, int eventId) throws SQLException {
        deleteIfColumnExists(connection, "UserEvent", "EventID", eventId);
        deleteIfColumnExists(connection, "SpecialTickets", "EventID", eventId);
        deleteCustomerTicketsForEvent(connection, eventId);
        deleteIfColumnExists(connection, "Tickets", "EventID", eventId);
        deleteIfColumnExists(connection, "SoldTickets", "EventName", event.getTitle());
    }

    private void deleteCustomerTicketsForEvent(Connection connection, int eventId) throws SQLException {
        if (!tableExists(connection, "CustomerTickets")
                || !tableExists(connection, "Tickets")
                || !columnExists(connection, "CustomerTickets", "TicketID")
                || !columnExists(connection, "Tickets", "TicketID")
                || !columnExists(connection, "Tickets", "EventID")) {
            return;
        }

        String sql = """
                DELETE ct
                FROM CustomerTickets ct
                JOIN Tickets t ON t.TicketID = ct.TicketID
                WHERE t.EventID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            statement.executeUpdate();
        }
    }

    private void deleteIfColumnExists(Connection connection, String tableName, String columnName, Object value) throws SQLException {
        if (!tableExists(connection, tableName) || !columnExists(connection, tableName, columnName)) {
            return;
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            statement.executeUpdate();
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = """
                SELECT 1
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
                SELECT 1
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
