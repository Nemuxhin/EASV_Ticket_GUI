package easv.dal;

import easv.be.SpecialTicketRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SpecialTicketDAO {

    public void createSpecialTicket(String specialTicketName,
                                    String description,
                                    String eventTitle,
                                    String publicCode,
                                    String price,
                                    int quantity,
                                    boolean validForAllEvents) {
        ensureTableExists();

        String sql = """
                INSERT INTO SpecialTickets (SpecialTicketName, Description, EventID, PublicCode, Price, Quantity, ValidForAllEvents, IsActive)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            Integer eventId = findEventIdByTitle(connection, eventTitle);

            statement.setString(1, specialTicketName);
            statement.setString(2, blankToNull(description));
            if (eventId == null) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, eventId);
            }
            statement.setString(4, publicCode);
            statement.setBigDecimal(5, parsePrice(price));
            statement.setInt(6, quantity);
            statement.setBoolean(7, validForAllEvents);
            statement.setBoolean(8, true);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not save special ticket.", ex);
        }
    }

    public boolean deactivateSpecialTicket(String publicCode) {
        ensureTableExists();

        String sql = """
                UPDATE SpecialTickets
                SET IsActive = 0
                WHERE PublicCode = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, publicCode);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not deactivate special ticket.", ex);
        }
    }

    public List<SpecialTicketRecord> getAllSpecialTickets() {
        ensureTableExists();

        String sql = """
                SELECT st.SpecialTicketID,
                       st.SpecialTicketName,
                       st.Description,
                       st.PublicCode,
                       st.Price,
                       st.Quantity,
                       st.ValidForAllEvents,
                       st.IsActive,
                       e.Name AS EventName
                FROM SpecialTickets st
                LEFT JOIN Events e ON e.EventID = st.EventID
                ORDER BY st.SpecialTicketID DESC
                """;

        List<SpecialTicketRecord> records = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                records.add(new SpecialTicketRecord(
                        resultSet.getInt("SpecialTicketID"),
                        resultSet.getString("SpecialTicketName"),
                        resultSet.getString("Description"),
                        resultSet.getString("EventName"),
                        resultSet.getString("PublicCode"),
                        formatPrice(resultSet.getBigDecimal("Price")),
                        resultSet.getInt("Quantity"),
                        resultSet.getBoolean("ValidForAllEvents"),
                        resultSet.getBoolean("IsActive")
                ));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load special tickets.", ex);
        }

        return records;
    }

    private void ensureTableExists() {
        String sql = """
                IF OBJECT_ID(N'dbo.SpecialTickets', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.SpecialTickets (
                        SpecialTicketID INT IDENTITY(1,1) PRIMARY KEY,
                        SpecialTicketName NVARCHAR(255) NOT NULL,
                        Description NVARCHAR(500) NULL,
                        EventID INT NULL,
                        PublicCode NVARCHAR(255) NOT NULL UNIQUE,
                        Price DECIMAL(10,2) NOT NULL,
                        Quantity INT NOT NULL CONSTRAINT DF_SpecialTickets_Quantity DEFAULT 1,
                        ValidForAllEvents BIT NOT NULL CONSTRAINT DF_SpecialTickets_ValidForAllEvents DEFAULT 0,
                        IsActive BIT NOT NULL CONSTRAINT DF_SpecialTickets_IsActive DEFAULT 1,
                        CONSTRAINT FK_SpecialTickets_Events
                            FOREIGN KEY (EventID) REFERENCES dbo.Events(EventID)
                    );
                END
                ELSE IF COL_LENGTH(N'dbo.SpecialTickets', N'Description') IS NULL
                BEGIN
                    ALTER TABLE dbo.SpecialTickets ADD Description NVARCHAR(500) NULL;
                END
                IF COL_LENGTH(N'dbo.SpecialTickets', N'Quantity') IS NULL
                BEGIN
                    ALTER TABLE dbo.SpecialTickets ADD Quantity INT NOT NULL CONSTRAINT DF_SpecialTickets_Quantity DEFAULT 1;
                END
                IF COL_LENGTH(N'dbo.SpecialTickets', N'IsActive') IS NULL
                BEGIN
                    ALTER TABLE dbo.SpecialTickets ADD IsActive BIT NOT NULL CONSTRAINT DF_SpecialTickets_IsActive DEFAULT 1;
                END
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not create special tickets table.", ex);
        }
    }

    private Integer findEventIdByTitle(Connection connection, String eventTitle) throws SQLException {
        if (eventTitle == null || eventTitle.isBlank() || "All Events".equalsIgnoreCase(eventTitle.trim())) {
            return null;
        }

        String sql = """
                SELECT TOP 1 EventID
                FROM Events
                WHERE Name = ?
                ORDER BY EventID DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventTitle.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("EventID");
                }
            }
        }

        return null;
    }

    public SpecialTicketRecord findByPublicCode(String publicCode) {
        ensureTableExists();

        String sql = """
                SELECT st.SpecialTicketID,
                       st.SpecialTicketName,
                       st.Description,
                       st.PublicCode,
                       st.Price,
                       st.Quantity,
                       st.ValidForAllEvents,
                       st.IsActive,
                       e.Name AS EventName
                FROM SpecialTickets st
                LEFT JOIN Events e ON e.EventID = st.EventID
                WHERE st.PublicCode = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, publicCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new SpecialTicketRecord(
                            resultSet.getInt("SpecialTicketID"),
                            resultSet.getString("SpecialTicketName"),
                            resultSet.getString("Description"),
                            resultSet.getString("EventName"),
                            resultSet.getString("PublicCode"),
                            formatPrice(resultSet.getBigDecimal("Price")),
                            resultSet.getInt("Quantity"),
                            resultSet.getBoolean("ValidForAllEvents"),
                            resultSet.getBoolean("IsActive")
                    );
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find special ticket.", ex);
        }

        return null;
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

        if (cleaned.isBlank()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(cleaned);
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
