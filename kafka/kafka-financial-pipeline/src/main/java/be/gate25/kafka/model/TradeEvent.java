package be.gate25.kafka.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a raw trade event emitted by the trading front-end.
 * This is the inbound message consumed from the {@code trade-events} topic.
 */
public class TradeEvent {

    private String  eventId;    // UUID — deduplication key
    private String  symbol;     // e.g. "BEL20:UCB"
    private Side    side;       // BUY or SELL
    private int     quantity;
    private double  price;
    private String  currency;   // e.g. "EUR"
    private String  traderId;   // e.g. "T-0042"
    private Instant timestamp;

    public enum Side { BUY, SELL }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public TradeEvent() {
        // required by Jackson
    }

    public TradeEvent(String symbol, Side side, int quantity,
                      double price, String currency, String traderId) {
        this.eventId   = UUID.randomUUID().toString();
        this.symbol    = symbol;
        this.side      = side;
        this.quantity  = quantity;
        this.price     = price;
        this.currency  = currency;
        this.traderId  = traderId;
        this.timestamp = Instant.now();
    }

    // -------------------------------------------------------------------------
    // Getters & setters
    // -------------------------------------------------------------------------

    public String getEventId()              { return eventId; }
    public void   setEventId(String id)     { this.eventId = id; }

    public String getSymbol()               { return symbol; }
    public void   setSymbol(String symbol)  { this.symbol = symbol; }

    public Side   getSide()                 { return side; }
    public void   setSide(Side side)        { this.side = side; }

    public int    getQuantity()             { return quantity; }
    public void   setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice()                { return price; }
    public void   setPrice(double price)    { this.price = price; }

    public String getCurrency()             { return currency; }
    public void   setCurrency(String c)     { this.currency = c; }

    public String getTraderId()             { return traderId; }
    public void   setTraderId(String id)    { this.traderId = id; }

    public Instant getTimestamp()              { return timestamp; }
    public void    setTimestamp(Instant ts)    { this.timestamp = ts; }

    @Override
    public String toString() {
        return "TradeEvent{eventId='%s', symbol='%s', side=%s, quantity=%d, price=%.2f, currency='%s', traderId='%s'}"
                .formatted(eventId, symbol, side, quantity, price, currency, traderId);
    }
}
