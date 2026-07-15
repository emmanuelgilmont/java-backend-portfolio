package be.gate25.batch.config;

import be.gate25.batch.domain.Settlement;
import be.gate25.batch.domain.TradeRecord;
import be.gate25.batch.listener.JobCompletionListener;
import be.gate25.batch.processor.SettlementProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Batch job definition for the EOD trade settlement batch.
 *
 * <p>Pipeline:
 * <pre>
 *   trades_YYYYMMDD.csv
 *       → FlatFileItemReader&lt;TradeRecord&gt;   (read, 1 item at a time)
 *       → SettlementProcessor                (filter CANCELLED, compute T+2)
 *       → CompositeItemWriter&lt;Settlement&gt;   (write chunk to DB + CSV)
 * </pre>
 *
 * <p>Note: do NOT annotate this class with {@code @EnableBatchProcessing}.
 * In Spring Boot 3 / Spring Batch 5, that annotation disables auto-configuration
 * and prevents {@code JobRepository} and {@code PlatformTransactionManager} from
 * being wired automatically.
 */
@Configuration
public class BatchConfig {

    // -------------------------------------------------------------------------
    // Job
    // -------------------------------------------------------------------------

    @Bean
    public Job settlementJob(JobRepository jobRepository,
                             Step settlementStep,
                             JobCompletionListener listener) {
        return new JobBuilder("settlementJob", jobRepository)
                .listener(listener)
                .start(settlementStep)
                .build();
    }

    // -------------------------------------------------------------------------
    // Step
    // -------------------------------------------------------------------------

    @Bean
    public Step settlementStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               FlatFileItemReader<TradeRecord> tradeReader,
                               SettlementProcessor processor,
                               CompositeItemWriter<Settlement> compositeWriter) {
        return new StepBuilder("settlementStep", jobRepository)
                .<TradeRecord, Settlement>chunk(10, transactionManager)
                .reader(tradeReader)
                .processor(processor)
                .writer(compositeWriter)
                .build();
    }

    // -------------------------------------------------------------------------
    // Reader — @StepScope: inputFile resolved from job parameters at step start
    // -------------------------------------------------------------------------

    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public FlatFileItemReader<TradeRecord> tradeReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {

        return new FlatFileItemReaderBuilder<TradeRecord>()
                .name("tradeReader")
                .resource(new FileSystemResource(inputFile))
                .linesToSkip(1) // skip CSV header row
                .delimited()
                .names("tradeId", "symbol", "side", "quantity", "price", "currency", "tradeDate", "status")
                .fieldSetMapper(fieldSet -> {
                    TradeRecord record = new TradeRecord();
                    record.setTradeId(fieldSet.readString("tradeId"));
                    record.setSymbol(fieldSet.readString("symbol"));
                    record.setSide(fieldSet.readString("side"));
                    record.setQuantity(fieldSet.readInt("quantity"));
                    record.setPrice(new BigDecimal(fieldSet.readString("price")));
                    record.setCurrency(fieldSet.readString("currency"));
                    record.setTradeDate(LocalDate.parse(fieldSet.readString("tradeDate")));
                    record.setStatus(fieldSet.readString("status"));
                    return record;
                })
                .build();
    }

    // -------------------------------------------------------------------------
    // Writers
    // -------------------------------------------------------------------------

    /**
     * Database writer — inserts settlements into the {@code settlements} table.
     * Named parameters ({@code :tradeId}, etc.) are resolved from getter names
     * via {@code BeanPropertyItemSqlParameterSourceProvider}.
     */
    @Bean
    public JdbcBatchItemWriter<Settlement> dbWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Settlement>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO settlements
                            (trade_id, symbol, side, quantity, price, settlement_amount, currency,
                             trade_date, settlement_date, status, processed_at)
                        VALUES
                            (:tradeId, :symbol, :side, :quantity, :price, :settlementAmount, :currency,
                             :tradeDate, :settlementDate, :status, :processedAt)
                        """)
                .beanMapped()
                .build();
    }

    /**
     * CSV writer — appends settlements to the daily report file.
     * Step-scoped because the output path depends on job parameters.
     */
    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public FlatFileItemWriter<Settlement> csvWriter(
            @Value("#{jobParameters['outputDir']}") String outputDir,
            @Value("#{jobParameters['runDate']}") String runDate) {

        String outputPath = outputDir + "/settlements_" + runDate + ".csv";

        BeanWrapperFieldExtractor<Settlement> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{
                "tradeId", "symbol", "side", "quantity", "price", "settlementAmount",
                "currency", "tradeDate", "settlementDate", "status", "processedAt"
        });

        DelimitedLineAggregator<Settlement> aggregator = new DelimitedLineAggregator<>();
        aggregator.setFieldExtractor(extractor);

        return new FlatFileItemWriterBuilder<Settlement>()
                .name("settlementCsvWriter")
                .resource(new FileSystemResource(outputPath))
                .lineAggregator(aggregator)
                .headerCallback(w -> w.write(
                        "tradeId,symbol,side,quantity,price,settlementAmount," +
                        "currency,tradeDate,settlementDate,status,processedAt"))
                .build();
    }

    /**
     * Composite writer — delegates to both the DB writer and the CSV writer.
     * Step-scoped because the CSV writer (one of the delegates) is step-scoped.
     */
    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public CompositeItemWriter<Settlement> compositeWriter(
            JdbcBatchItemWriter<Settlement> dbWriter,
            FlatFileItemWriter<Settlement> csvWriter) {

        CompositeItemWriter<Settlement> writer = new CompositeItemWriter<>();
        writer.setDelegates(List.of(dbWriter, csvWriter));
        return writer;
    }
}
