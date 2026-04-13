package easv.bll;

import easv.be.Event;
import easv.be.TicketPurchase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventManagerTest {

    private final EventManager eventManager = new EventManager();

    private Event createEventWithPrice(String price) {
        return new Event(
                "Spring Party",
                "20 Apr 2026 at 18:00",
                "",
                "EASV Hall",
                "",
                "Party night",
                price,
                "100",
                "Available",
                new String[0]
        );
    }

    @Test
    @DisplayName("validateEvent rejects blank title")
    void validateEvent_shouldRejectBlankTitle() {
        String result = eventManager.validateEvent(
                "",
                "20/04/2026",
                "18:00",
                "EASV Hall",
                "100",
                "150"
        );

        assertEquals("Please enter an event title.", result);
    }

    @Test
    @DisplayName("validateEvent rejects missing date")
    void validateEvent_shouldRejectMissingDate() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "",
                "18:00",
                "EASV Hall",
                "100",
                "150"
        );

        assertEquals("Please enter an event date.", result);
    }

    @Test
    @DisplayName("validateEvent rejects invalid date format")
    void validateEvent_shouldRejectInvalidDateFormat() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "2026-04-20",
                "18:00",
                "EASV Hall",
                "100",
                "150"
        );

        assertEquals("Please enter a valid event date in the format dd/MM/yyyy.", result);
    }

    @Test
    @DisplayName("validateEvent rejects missing time")
    void validateEvent_shouldRejectMissingTime() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "",
                "EASV Hall",
                "100",
                "150"
        );

        assertEquals("Please enter an event time.", result);
    }

    @Test
    @DisplayName("validateEvent rejects invalid time format")
    void validateEvent_shouldRejectInvalidTimeFormat() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "6 PM",
                "EASV Hall",
                "100",
                "150"
        );

        assertEquals("Please enter a valid event time in the format HH:mm.", result);
    }

    @Test
    @DisplayName("validateEvent rejects blank venue")
    void validateEvent_shouldRejectBlankVenue() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "18:00",
                "",
                "100",
                "150"
        );

        assertEquals("Please enter a venue.", result);
    }

    @Test
    @DisplayName("validateEvent rejects blank capacity")
    void validateEvent_shouldRejectBlankCapacity() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "18:00",
                "EASV Hall",
                "",
                "150"
        );

        assertEquals("Please enter the event capacity.", result);
    }

    @Test
    @DisplayName("validateEvent rejects non numeric capacity")
    void validateEvent_shouldRejectNonNumericCapacity() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "18:00",
                "EASV Hall",
                "abc",
                "150"
        );

        assertEquals("Capacity must be a whole number.", result);
    }

    @Test
    @DisplayName("validateEvent rejects capacity below one")
    void validateEvent_shouldRejectCapacityBelowOne() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "18:00",
                "EASV Hall",
                "0",
                "150"
        );

        assertEquals("Capacity must be at least 1.", result);
    }

    @Test
    @DisplayName("validateEvent rejects blank price")
    void validateEvent_shouldRejectBlankPrice() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "18:00",
                "EASV Hall",
                "100",
                ""
        );

        assertEquals("Please enter the ticket price.", result);
    }

    @Test
    @DisplayName("validateEvent rejects non numeric price")
    void validateEvent_shouldRejectNonNumericPrice() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "18:00",
                "EASV Hall",
                "100",
                "abc"
        );

        assertEquals("Price must be a number.", result);
    }

    @Test
    @DisplayName("validateEvent rejects negative price")
    void validateEvent_shouldRejectNegativePrice() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "18:00",
                "EASV Hall",
                "100",
                "-50"
        );

        assertEquals("Price cannot be negative.", result);
    }

    @Test
    @DisplayName("validateEvent accepts valid input")
    void validateEvent_shouldAcceptValidInput() {
        String result = eventManager.validateEvent(
                "Spring Party",
                "20/04/2026",
                "18:00",
                "EASV Hall",
                "100",
                "150"
        );

        assertNull(result);
    }

    @Test
    @DisplayName("validatePurchase rejects blank customer name")
    void validatePurchase_shouldRejectBlankCustomerName() {
        String result = eventManager.validatePurchase(
                "",
                "alice@example.com",
                "VIP",
                1
        );

        assertEquals("Please enter the customer's full name.", result);
    }

    @Test
    @DisplayName("validatePurchase rejects blank email")
    void validatePurchase_shouldRejectBlankEmail() {
        String result = eventManager.validatePurchase(
                "Alice Jensen",
                "",
                "VIP",
                1
        );

        assertEquals("Please enter the customer's email address.", result);
    }

    @Test
    @DisplayName("validatePurchase rejects invalid email")
    void validatePurchase_shouldRejectInvalidEmail() {
        String result = eventManager.validatePurchase(
                "Alice Jensen",
                "aliceexample.com",
                "VIP",
                1
        );

        assertEquals("Please enter a valid email address.", result);
    }

    @Test
    @DisplayName("validatePurchase rejects blank ticket type")
    void validatePurchase_shouldRejectBlankTicketType() {
        String result = eventManager.validatePurchase(
                "Alice Jensen",
                "alice@example.com",
                "",
                1
        );

        assertEquals("Please select a ticket type.", result);
    }

    @Test
    @DisplayName("validatePurchase rejects quantity below one")
    void validatePurchase_shouldRejectQuantityBelowOne() {
        String result = eventManager.validatePurchase(
                "Alice Jensen",
                "alice@example.com",
                "VIP",
                0
        );

        assertEquals("Quantity must be at least 1.", result);
    }

    @Test
    @DisplayName("validatePurchase accepts valid purchase input")
    void validatePurchase_shouldAcceptValidInput() {
        String result = eventManager.validatePurchase(
                "Alice Jensen",
                "alice@example.com",
                "VIP",
                2
        );

        assertNull(result);
    }

    @Test
    @DisplayName("isValidEmail accepts valid email")
    void isValidEmail_shouldAcceptValidEmail() {
        assertTrue(eventManager.isValidEmail("alice@example.com"));
    }

    @Test
    @DisplayName("isValidEmail rejects missing at sign")
    void isValidEmail_shouldRejectMissingAtSign() {
        assertFalse(eventManager.isValidEmail("aliceexample.com"));
    }

    @Test
    @DisplayName("isValidEmail rejects blank email")
    void isValidEmail_shouldRejectBlankEmail() {
        assertFalse(eventManager.isValidEmail("   "));
    }

    @Test
    @DisplayName("isValidEmail rejects null email")
    void isValidEmail_shouldRejectNullEmail() {
        assertFalse(eventManager.isValidEmail(null));
    }

    @Test
    @DisplayName("calculateTotalPrice uses standard price for standard ticket")
    void calculateTotalPrice_shouldUseBasePriceForStandardTicket() {
        Event event = createEventWithPrice("100 DKK");

        double total = eventManager.calculateTotalPrice(event, "Standard", 2);

        assertEquals(200.0, total);
    }

    @Test
    @DisplayName("calculateTotalPrice applies VIP multiplier")
    void calculateTotalPrice_shouldApplyVipMultiplier() {
        Event event = createEventWithPrice("100 DKK");

        double total = eventManager.calculateTotalPrice(event, "VIP", 2);

        assertEquals(300.0, total);
    }

    @Test
    @DisplayName("calculateTotalPrice applies Student discount")
    void calculateTotalPrice_shouldApplyStudentDiscount() {
        Event event = createEventWithPrice("100 DKK");

        double total = eventManager.calculateTotalPrice(event, "Student", 2);

        assertEquals(140.0, total);
    }

    @Test
    @DisplayName("calculateTotalPrice handles free event price")
    void calculateTotalPrice_shouldHandleFreeEventPrice() {
        Event event = createEventWithPrice("Free");

        double total = eventManager.calculateTotalPrice(event, "VIP", 3);

        assertEquals(0.0, total);
    }

    @Test
    @DisplayName("calculateTotalPrice handles decimal prices")
    void calculateTotalPrice_shouldHandleDecimalPrices() {
        Event event = createEventWithPrice("99.50 DKK");

        double total = eventManager.calculateTotalPrice(event, "Student", 2);

        assertEquals(139.3, total, 0.0001);
    }

    @Test
    @DisplayName("createTicketPurchase preserves customer details and quantity")
    void createTicketPurchase_shouldPreserveCustomerDetailsAndQuantity() {
        Event event = createEventWithPrice("100 DKK");

        TicketPurchase purchase = eventManager.createTicketPurchase(
                event,
                "Alice Jensen",
                "alice@example.com",
                "VIP",
                2
        );

        assertEquals(event, purchase.getEvent());
        assertEquals("Alice Jensen", purchase.getCustomerName());
        assertEquals("alice@example.com", purchase.getCustomerEmail());
        assertEquals("VIP", purchase.getTicketType());
        assertEquals(2, purchase.getQuantity());
    }

    @Test
    @DisplayName("createTicketPurchase calculates total price correctly")
    void createTicketPurchase_shouldCalculateTotalPriceCorrectly() {
        Event event = createEventWithPrice("100 DKK");

        TicketPurchase purchase = eventManager.createTicketPurchase(
                event,
                "Alice Jensen",
                "alice@example.com",
                "Student",
                3
        );

        assertEquals(210.0, purchase.getTotalPrice());
    }

    @Test
    @DisplayName("createTicketPurchase normalizes ticket type to uppercase")
    void createTicketPurchase_shouldNormalizeTicketTypeToUppercase() {
        Event event = createEventWithPrice("100 DKK");

        TicketPurchase purchase = eventManager.createTicketPurchase(
                event,
                "Alice Jensen",
                "alice@example.com",
                "vip",
                1
        );

        assertEquals("VIP", purchase.getTicketType());
    }

    @Test
    @DisplayName("createTicketPurchase trims customer name and email")
    void createTicketPurchase_shouldTrimCustomerFields() {
        Event event = createEventWithPrice("100 DKK");

        TicketPurchase purchase = eventManager.createTicketPurchase(
                event,
                "  Alice Jensen  ",
                "  alice@example.com  ",
                "Standard",
                1
        );

        assertEquals("Alice Jensen", purchase.getCustomerName());
        assertEquals("alice@example.com", purchase.getCustomerEmail());
    }
}
