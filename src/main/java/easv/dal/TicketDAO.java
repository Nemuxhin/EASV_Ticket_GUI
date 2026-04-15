package easv.dal;

import easv.be.Ticket;
import easv.be.Event;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TicketDAO {

    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter FORM_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm", Locale.ENGLISH);
    private static final List<Ticket> tickets = new ArrayList<>();
    private static final Map<String, LinkedHashMap<String, String>> eventTicketTypes = new LinkedHashMap<>();

    public void addTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null.");
        }
        tickets.add(ticket);
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets);
    }

    public Ticket findByToken(String secureToken) {
        if (secureToken == null || secureToken.isBlank()) {
            return null;
        }

        for (Ticket ticket : tickets) {
            if (secureToken.equals(ticket.getSecureToken())) {
                return ticket;
            }
        }
        return null;
    }

    public List<Ticket> findByGroupId(String ticketGroupId) {
        List<Ticket> result = new ArrayList<>();

        if (ticketGroupId == null || ticketGroupId.isBlank()) {
            return result;
        }

        for (Ticket ticket : tickets) {
            if (ticketGroupId.equals(ticket.getTicketGroupId())) {
                result.add(ticket);
            }
        }

        return result;
    }

    public boolean updateTicket(Ticket updatedTicket) {
        if (updatedTicket == null || updatedTicket.getTicketId() == null) {
            return false;
        }

        for (int i = 0; i < tickets.size(); i++) {
            Ticket currentTicket = tickets.get(i);

            if (updatedTicket.getTicketId().equals(currentTicket.getTicketId())) {
                tickets.set(i, updatedTicket);
                return true;
            }
        }

        return false;
    }

    public LinkedHashMap<String, String> getTicketTypesForEvent(Event event) {
        if (event == null) {
            return new LinkedHashMap<>();
        }

        Integer eventId = findEventId(event);
        if (eventId == null) {
            LinkedHashMap<String, String> fallback = eventTicketTypes.get(buildEventKey(event));
            return fallback == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fallback);
        }

        LinkedHashMap<String, String> storedTypes = new LinkedHashMap<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(buildSelectTicketTypesSql(connection))) {
            statement.setInt(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    storedTypes.put(
                            resultSet.getString("TicketTypeValue"),
                            formatPrice(resultSet.getBigDecimal("Price"))
                    );
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load ticket types.", ex);
        }

        eventTicketTypes.put(buildEventKey(event), new LinkedHashMap<>(storedTypes));
        return storedTypes;
    }

    public void setTicketTypesForEvent(Event event, LinkedHashMap<String, String> ticketTypes) {
        if (event == null) {
            return;
        }

        Integer eventId = findEventId(event);
        if (eventId == null) {
            if (ticketTypes == null || ticketTypes.isEmpty()) {
                eventTicketTypes.remove(buildEventKey(event));
            } else {
                eventTicketTypes.put(buildEventKey(event), new LinkedHashMap<>(ticketTypes));
            }
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            String ticketTypeColumn = resolveTicketTypeColumn(connection);
            LinkedHashMap<String, Integer> existingRows = loadExistingTicketTypeRows(connection, eventId, ticketTypeColumn);

            if (ticketTypes != null && !ticketTypes.isEmpty()) {
                try (PreparedStatement updateStatement = connection.prepareStatement(buildUpdateTicketTypesSql(ticketTypeColumn));
                     PreparedStatement insertStatement = connection.prepareStatement(buildInsertTicketTypesSql(connection))) {
                    for (Map.Entry<String, String> entry : ticketTypes.entrySet()) {
                        Integer ticketId = existingRows.remove(entry.getKey());

                        if (ticketId != null) {
                            updateStatement.setString(1, entry.getKey());
                            updateStatement.setBigDecimal(2, parsePrice(entry.getValue()));
                            updateStatement.setInt(3, ticketId);
                            updateStatement.addBatch();
                            continue;
                        }

                        insertStatement.setString(1, UUID.randomUUID().toString());
                        insertStatement.setString(2, entry.getKey());
                        insertStatement.setInt(3, eventId);
                        insertStatement.setBigDecimal(4, parsePrice(entry.getValue()));
                        insertStatement.addBatch();
                    }

                    updateStatement.executeBatch();
                    insertStatement.executeBatch();
                }
            }

            deleteRemovedTicketTypes(connection, existingRows);

            connection.commit();
            eventTicketTypes.put(buildEventKey(event), new LinkedHashMap<>(ticketTypes == null ? new LinkedHashMap<>() : ticketTypes));
        } catch (SQLException ex) {
            throw new RuntimeException("Could not save ticket types.", ex);
        }
    }

    public void removeTicketTypesForEvent(Event event) {
        if (event == null) {
            return;
        }

        Integer eventId = findEventId(event);
        if (eventId != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);
                String ticketTypeColumn = resolveTicketTypeColumn(connection);
                LinkedHashMap<String, Integer> existingRows = loadExistingTicketTypeRows(connection, eventId, ticketTypeColumn);
                deleteRemovedTicketTypes(connection, existingRows);
                connection.commit();
            } catch (SQLException ex) {
                throw new RuntimeException("Could not remove ticket types.", ex);
            }
        }

        eventTicketTypes.remove(buildEventKey(event));
    }

    public void moveTicketTypesToUpdatedEvent(Event oldEvent, Event updatedEvent) {
        if (oldEvent == null || updatedEvent == null) {
            return;
        }

        String oldKey = buildEventKey(oldEvent);
        LinkedHashMap<String, String> types = eventTicketTypes.remove(oldKey);
        if (types != null) {
            eventTicketTypes.put(buildEventKey(updatedEvent), types);
        }
    }

    public Integer findTicketTypeId(Event event, String ticketType) {
        if (event == null || ticketType == null || ticketType.isBlank()) {
            return null;
        }

        Integer eventId = findEventId(event);
        if (eventId == null) {
            return null;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String ticketTypeColumn = resolveTicketTypeColumn(connection);
            LinkedHashMap<String, Integer> existingRows = loadExistingTicketTypeRows(connection, eventId, ticketTypeColumn);

            for (Map.Entry<String, Integer> entry : existingRows.entrySet()) {
                if (entry.getKey() != null && entry.getKey().trim().equalsIgnoreCase(ticketType.trim())) {
                    return entry.getValue();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find ticket type for event.", ex);
        }

        return null;
    }

    private String buildEventKey(Event event) {
        String title = event.getTitle() == null ? "" : event.getTitle().trim().toLowerCase();
        String location = event.getLocation() == null ? "" : event.getLocation().trim().toLowerCase();
        String start = event.getStartDateTime() == null ? "" : event.getStartDateTime().trim().toLowerCase();
        return title + "|" + location + "|" + start;
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
            throw new RuntimeException("Could not find event for ticket types.", ex);
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

    private String buildSelectTicketTypesSql(Connection connection) throws SQLException {
        String ticketTypeColumn = resolveTicketTypeColumn(connection);
        return """
                SELECT %s AS TicketTypeValue, Price
                FROM Tickets
                WHERE EventID = ?
                ORDER BY TicketID
                """.formatted(ticketTypeColumn);
    }

    private String buildInsertTicketTypesSql(Connection connection) throws SQLException {
        String ticketTypeColumn = resolveTicketTypeColumn(connection);
        return """
                INSERT INTO Tickets (PublicCode, %s, EventID, Price)
                VALUES (?, ?, ?, ?)
                """.formatted(ticketTypeColumn);
    }

    private String buildUpdateTicketTypesSql(String ticketTypeColumn) {
        return """
                UPDATE Tickets
                SET %s = ?, Price = ?
                WHERE TicketID = ?
                """.formatted(ticketTypeColumn);
    }

    private String resolveTicketTypeColumn(Connection connection) throws SQLException {
        for (String candidate : new String[]{"TicketType", "TicketsType", "TicketTypes"}) {
            if (columnExists(connection, "Tickets", candidate)) {
                return candidate;
            }
        }

        throw new SQLException("Could not find the ticket type column in dbo.Tickets.");
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

    private LinkedHashMap<String, Integer> loadExistingTicketTypeRows(Connection connection,
                                                                      int eventId,
                                                                      String ticketTypeColumn) throws SQLException {
        String sql = """
                SELECT TicketID, %s AS TicketTypeValue
                FROM Tickets
                WHERE EventID = ?
                ORDER BY TicketID
                """.formatted(ticketTypeColumn);

        LinkedHashMap<String, Integer> rows = new LinkedHashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.put(resultSet.getString("TicketTypeValue"), resultSet.getInt("TicketID"));
                }
            }
        }

        return rows;
    }

    private void deleteRemovedTicketTypes(Connection connection, LinkedHashMap<String, Integer> rowsToDelete) throws SQLException {
        if (rowsToDelete.isEmpty()) {
            return;
        }

        try (PreparedStatement deleteCustomerTickets = connection.prepareStatement("DELETE FROM CustomerTickets WHERE TicketID = ?");
             PreparedStatement deleteTickets = connection.prepareStatement("DELETE FROM Tickets WHERE TicketID = ?")) {
            for (Integer ticketId : rowsToDelete.values()) {
                deleteCustomerTickets.setInt(1, ticketId);
                deleteCustomerTickets.executeUpdate();

                deleteTickets.setInt(1, ticketId);
                deleteTickets.executeUpdate();
            }
        }
    }
}
