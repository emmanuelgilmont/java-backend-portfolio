package be.gate25.batch.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input record read from the daily trade CSV file.
 *
 * <p>Field order matches the CSV column order:
 * tradeId, symbol, side, quantity, price, currency, tradeDate, status
 */
@Data
public class TradeRecord {

    private String     tradeId;
    private String     symbol;

    /** BUY or SELL */
    private String     side;
    private int        quantity;
    private BigDecimal price;
    private String     currency;
    private LocalDate  tradeDate;

    /** PENDING or CANCELLED */
    private String     status;
}