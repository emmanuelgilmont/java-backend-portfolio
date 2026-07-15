package be.gate25.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EOD trade settlement batch application.
 *
 * <p>Jobs are not triggered automatically on startup ({@code spring.batch.job.enabled=false}).
 * Trigger a job run via command-line arguments or a scheduler:
 *
 * <pre>
 *   java -jar trade-settlement-batch.jar \
 *     --spring.batch.job.name=settlementJob \
 *     inputFile=/data/trades_20260521.csv \
 *     runDate=2026-05-21 \
 *     outputDir=/data/output
 * </pre>
 */
@SpringBootApplication
public class TradeBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeBatchApplication.class, args);
    }
}
