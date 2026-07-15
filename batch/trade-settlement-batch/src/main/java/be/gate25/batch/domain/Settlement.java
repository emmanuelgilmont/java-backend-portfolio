package be.gate25.batch.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Processed trade settlement.
 *
 * <p>Written to two destinations by the composite writer:
 * <ul>
 *   <li>the {@code settlements} PostgreSQL table (JdbcBatchItemWriter)</li>
 *   <li>the daily CSV report file (FlatFileItemWriter)</li>
 * </ul>
 *
 * <p>Property names match the SQL named parameters in the INSERT statement
 * ({@code :tradeId}, {@code :settlementAmount}, etc.).
 */
@Data
public class Settlement {

    private String           tradeId;
    private String           symbol;
    private String           side;
    private int              quantity;
    private BigDecimal       price;

    /** quantity × price — always BigDecimal, never double. */
    private BigDecimal       settlementAmount;
    private String           currency;
    private LocalDate        tradeDate;

    /** Standard T+2 settlement date. */
    private LocalDate        settlementDate;
    private SettlementStatus status;
    private LocalDateTime    processedAt;
}