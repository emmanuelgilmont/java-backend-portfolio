package be.gate25.batch.processor;

import be.gate25.batch.domain.Settlement;
import be.gate25.batch.domain.SettlementStatus;
import be.gate25.batch.domain.TradeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Converts a raw {@link TradeRecord} into a {@link Settlement}.
 *
 * <p>Business rules:
 * <ul>
 *   <li>CANCELLED trades → return {@code null} (Spring Batch filters them out, no write)</li>
 *   <li>PENDING trades → compute settlement amount (quantity × price) and T+2 date</li>
 * </ul>
 */
@Component
public class SettlementProcessor implements ItemProcessor<TradeRecord, Settlement> {

    private static final Logger log = LoggerFactory.getLogger(SettlementProcessor.class);

    @Override
    public Settlement process(TradeRecord trade) {
        if ("CANCELLED".equalsIgnoreCase(trade.getStatus())) {
            log.debug("Filtering cancelled trade: {}", trade.getTradeId());
            // Returning null tells Spring Batch to skip this item (no write, filterCount++)
            return null;
        }

        BigDecimal settlementAmount = trade.getPrice()
                .multiply(BigDecimal.valueOf(trade.getQuantity()));

        Settlement settlement = new Settlement();
        settlement.setTradeId(trade.getTradeId());
        settlement.setSymbol(trade.getSymbol());
        settlement.setSide(trade.getSide());
        settlement.setQuantity(trade.getQuantity());
        settlement.setPrice(trade.getPrice());
        settlement.setSettlementAmount(settlementAmount);
        settlement.setCurrency(trade.getCurrency());
        settlement.setTradeDate(trade.getTradeDate());
        settlement.setSettlementDate(trade.getTradeDate().plusDays(2)); // T+2
        settlement.setStatus(SettlementStatus.SETTLED);
        settlement.setProcessedAt(LocalDateTime.now());

        log.info("Processed {} {} {} @ {} → settlement amount {} {}",
                trade.getSide(), trade.getQuantity(), trade.getSymbol(),
                trade.getPrice(), settlementAmount, trade.getCurrency());

        return settlement;
    }
}
