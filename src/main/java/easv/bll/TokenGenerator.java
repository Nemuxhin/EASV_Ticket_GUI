package easv.bll;

import java.util.UUID;

public class TokenGenerator {

    public String generateTicketId() {
        return UUID.randomUUID().toString();
    }

    public String generateSecureToken() {
        return UUID.randomUUID().toString();
    }

    public String generateCustomerId() {
        return UUID.randomUUID().toString();
    }
}