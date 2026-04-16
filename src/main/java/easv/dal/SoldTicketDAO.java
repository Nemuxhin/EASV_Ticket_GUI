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

        ensureTableExists();

        if (existsByPublicCode(ticket.getSecureToken())) {
            return;
        }

        Customer customer = ticket.getCustomer();

        String customerName;
        String customerEmail;

        if (customer != null) {
            customerName = blankToNull(customer.getName());
            customerEmail = blankToNull(customer.getEmail());
        } else if (ticket.isSpecialTicket()) {
            customerName = "Special Ticket";
            customerEmail = "not-applicable@local";
        } else {
            customerName = "Unknown Customer";
            customerEmail = "unknown@local";
        }

        String sql = """
                INSERT INTO SoldTickets (
                    TicketId,
                    EventName,
                    EventStartDateTime,
                    EventEndDateTime,
                    EventLocation,
                    EventLocationGuidance,
                    EventNotes,
                    CustomerName,
                    CustomerEmail,
                    TicketType,
                    TicketDescription,
                    Price,
                    IsUsed,
                    PublicCode,
                    IsSpecialTicket,
                    ValidForAllEvents
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, blankToNull(ticket.getTicketId()));
            statement.setString(2, blankToNull(ticket.getEventTitle()));
            statement.setString(3, blankToNull(ticket.getEventStartDateTime()));
            statement.setString(4, blankToNull(ticket.getEventEndDateTime()));
            statement.setString(5, blankToNull(ticket.getEventLocation()));
            statement.setString(6, blankToNull(ticket.getEventLocationGuidance()));
            statement.setString(7, blankToNull(ticket.getEventNotes()));
            statement.setString(8, customerName);
            statement.setString(9, customerEmail);
            statement.setString(10, blankToNull(ticket.getTicketType()));
            statement.setString(11, blankToNull(ticket.getTicketDescription()));
            statement.setBigDecimal(12, parsePrice(ticket.getPrice()));
            statement.setBoolean(13, ticket.isUsed());
            statement.setString(14, blankToNull(ticket.getSecureToken()));
            statement.setBoolean(15, ticket.isSpecialTicket());
            statement.setBoolean(16, ticket.isValidForAllEvents());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not save sold ticket.", ex);
        }
    }

    public List<SoldTicketRecord> getAllSoldTickets() {
        ensureTableExists();

        String sql = """
                SELECT
                    SoldTicketID,
                    TicketId,
                    EventName,
                    EventStartDateTime,
                    EventEndDateTime,
                    EventLocation,
                    EventLocationGuidance,
                    EventNotes,
                    CustomerName,
                    CustomerEmail,
                    TicketType,
                    TicketDescription,
                    Price,
                    IsUsed,
                    PublicCode,
                    IsSpecialTicket,
                    ValidForAllEvents
                FROM SoldTickets
                ORDER BY SoldTicketID DESC
                """;

        List<SoldTicketRecord> records = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                records.add(mapRecord(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load sold tickets.", ex);
        }

        return records;
    }

    public List<SoldTicketRecord> getRecentSoldTickets(int limit) {
        ensureTableExists();

        int safeLimit = Math.max(1, limit);

        String sql = """
                SELECT TOP (?)
                    SoldTicketID,
                    TicketId,
                    EventName,
                    EventStartDateTime,
                    EventEndDateTime,
                    EventLocation,
                    EventLocationGuidance,
                    EventNotes,
                    CustomerName,
                    CustomerEmail,
                    TicketType,
                    TicketDescription,
                    Price,
                    IsUsed,
                    PublicCode,
                    IsSpecialTicket,
                    ValidForAllEvents
                FROM SoldTickets
                ORDER BY SoldTicketID DESC
                """;

        List<SoldTicketRecord> records = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, safeLimit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapRecord(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load recent sold tickets.", ex);
        }

        return records;
    }

    public List<SoldTicketRecord> searchSoldTickets(String query, int limit) {
        ensureTableExists();

        String needle = query == null ? "" : query.trim();
        if (needle.isBlank()) {
            return getRecentSoldTickets(limit);
        }

        int safeLimit = Math.max(1, limit);
        String likeValue = "%" + needle + "%";

        String sql = """
                SELECT TOP (?)
                    SoldTicketID,
                    TicketId,
                    EventName,
                    EventStartDateTime,
                    EventEndDateTime,
                    EventLocation,
                    EventLocationGuidance,
                    EventNotes,
                    CustomerName,
                    CustomerEmail,
                    TicketType,
                    TicketDescription,
                    Price,
                    IsUsed,
                    PublicCode,
                    IsSpecialTicket,
                    ValidForAllEvents
                FROM SoldTickets
                WHERE
                    ISNULL(TicketId, '') LIKE ?
                    OR ISNULL(EventName, '') LIKE ?
                    OR ISNULL(CustomerName, '') LIKE ?
                    OR ISNULL(CustomerEmail, '') LIKE ?
                    OR ISNULL(TicketType, '') LIKE ?
                    OR ISNULL(TicketDescription, '') LIKE ?
                    OR ISNULL(PublicCode, '') LIKE ?
                ORDER BY SoldTicketID DESC
                """;

        List<SoldTicketRecord> records = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, safeLimit);
            statement.setString(2, likeValue);
            statement.setString(3, likeValue);
            statement.setString(4, likeValue);
            statement.setString(5, likeValue);
            statement.setString(6, likeValue);
            statement.setString(7, likeValue);
            statement.setString(8, likeValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapRecord(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not search sold tickets.", ex);
        }

        return records;
    }

    public SoldTicketRecord findByPublicCode(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            return null;
        }

        ensureTableExists();

        String sql = """
                SELECT TOP 1
                    SoldTicketID,
                    TicketId,
                    EventName,
                    EventStartDateTime,
                    EventEndDateTime,
                    EventLocation,
                    EventLocationGuidance,
                    EventNotes,
                    CustomerName,
                    CustomerEmail,
                    TicketType,
                    TicketDescription,
                    Price,
                    IsUsed,
                    PublicCode,
                    IsSpecialTicket,
                    ValidForAllEvents
                FROM SoldTickets
                WHERE PublicCode = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, publicCode.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRecord(resultSet) : null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find sold ticket by public code.", ex);
        }
    }

    public SoldTicketRecord findByTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            return null;
        }

        ensureTableExists();

        String sql = """
                SELECT TOP 1
                    SoldTicketID,
                    TicketId,
                    EventName,
                    EventStartDateTime,
                    EventEndDateTime,
                    EventLocation,
                    EventLocationGuidance,
                    EventNotes,
                    CustomerName,
                    CustomerEmail,
                    TicketType,
                    TicketDescription,
                    Price,
                    IsUsed,
                    PublicCode,
                    IsSpecialTicket,
                    ValidForAllEvents
                FROM SoldTickets
                WHERE TicketId = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, ticketId.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRecord(resultSet) : null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find sold ticket by ticket ID.", ex);
        }
    }

    public SoldTicketRecord findByPublicCodeOrTicketId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        SoldTicketRecord byPublicCode = findByPublicCode(value);
        if (byPublicCode != null) {
            return byPublicCode;
        }

        return findByTicketId(value);
    }

    public boolean setUsedState(String publicCode, boolean used) {
        if (publicCode == null || publicCode.isBlank()) {
            return false;
        }

        ensureTableExists();

        String sql = """
                UPDATE SoldTickets
                SET IsUsed = ?
                WHERE PublicCode = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, used);
            statement.setString(2, publicCode.trim());
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

            statement.setString(1, publicCode.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not check sold ticket existence.", ex);
        }
    }

    private SoldTicketRecord mapRecord(ResultSet resultSet) throws SQLException {
        return new SoldTicketRecord(
                resultSet.getInt("SoldTicketID"),
                resultSet.getString("TicketId"),
                resultSet.getString("EventName"),
                resultSet.getString("EventStartDateTime"),
                resultSet.getString("EventEndDateTime"),
                resultSet.getString("EventLocation"),
                resultSet.getString("EventLocationGuidance"),
                resultSet.getString("EventNotes"),
                resultSet.getString("CustomerName"),
                resultSet.getString("CustomerEmail"),
                resultSet.getString("TicketType"),
                resultSet.getString("TicketDescription"),
                formatPrice(resultSet.getBigDecimal("Price")),
                resultSet.getBoolean("IsUsed"),
                resultSet.getString("PublicCode"),
                resultSet.getBoolean("IsSpecialTicket"),
                resultSet.getBoolean("ValidForAllEvents")
        );
    }

    private void ensureTableExists() {
        String sql = """
                IF OBJECT_ID(N'dbo.SoldTickets', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.SoldTickets (
                        SoldTicketID INT IDENTITY(1,1) PRIMARY KEY,
                        TicketId NVARCHAR(255) NULL,
                        EventName NVARCHAR(255) NULL,
                        EventStartDateTime NVARCHAR(255) NULL,
                        EventEndDateTime NVARCHAR(255) NULL,
                        EventLocation NVARCHAR(255) NULL,
                        EventLocationGuidance NVARCHAR(1000) NULL,
                        EventNotes NVARCHAR(MAX) NULL,
                        CustomerName NVARCHAR(255) NULL,
                        CustomerEmail NVARCHAR(255) NULL,
                        TicketType NVARCHAR(255) NOT NULL,
                        TicketDescription NVARCHAR(1000) NULL,
                        Price DECIMAL(10,2) NOT NULL,
                        IsUsed BIT NOT NULL CONSTRAINT DF_SoldTickets_IsUsed DEFAULT 0,
                        PublicCode NVARCHAR(255) NOT NULL UNIQUE,
                        IsSpecialTicket BIT NOT NULL CONSTRAINT DF_SoldTickets_IsSpecial DEFAULT 0,
                        ValidForAllEvents BIT NOT NULL CONSTRAINT DF_SoldTickets_ValidForAllEvents DEFAULT 0
                    );
                END

                IF COL_LENGTH('dbo.SoldTickets', 'TicketId') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD TicketId NVARCHAR(255) NULL;

                IF COL_LENGTH('dbo.SoldTickets', 'EventStartDateTime') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD EventStartDateTime NVARCHAR(255) NULL;

                IF COL_LENGTH('dbo.SoldTickets', 'EventEndDateTime') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD EventEndDateTime NVARCHAR(255) NULL;

                IF COL_LENGTH('dbo.SoldTickets', 'EventLocation') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD EventLocation NVARCHAR(255) NULL;

                IF COL_LENGTH('dbo.SoldTickets', 'EventLocationGuidance') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD EventLocationGuidance NVARCHAR(1000) NULL;

                IF COL_LENGTH('dbo.SoldTickets', 'EventNotes') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD EventNotes NVARCHAR(MAX) NULL;

                IF COL_LENGTH('dbo.SoldTickets', 'TicketDescription') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD TicketDescription NVARCHAR(1000) NULL;

                IF COL_LENGTH('dbo.SoldTickets', 'IsSpecialTicket') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD IsSpecialTicket BIT NOT NULL CONSTRAINT DF_SoldTickets_IsSpecial_Added DEFAULT 0;

                IF COL_LENGTH('dbo.SoldTickets', 'ValidForAllEvents') IS NULL
                    ALTER TABLE dbo.SoldTickets ADD ValidForAllEvents BIT NOT NULL CONSTRAINT DF_SoldTickets_ValidForAllEvents_Added DEFAULT 0;
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not create or update sold tickets table.", ex);
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
