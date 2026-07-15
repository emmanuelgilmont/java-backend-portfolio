package be.gate25.batch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Layer 2 — CI tests (Docker required).
 *
 * <p>Testcontainers starts a real PostgreSQL instance on a random port.
 * The test is automatically skipped if Docker is not available
 * ({@code @Testcontainers(disabledWithoutDocker = true)}).
 *
 * <p>What this layer adds on top of H2:
 * <ul>
 *   <li>Real PostgreSQL types and constraints</li>
 *   <li>Spring Batch BATCH_* tables on PostgreSQL (not H2-specific DDL)</li>
 *   <li>Settlements persisted and queryable via JdbcTemplate on real PostgreSQL</li>
 *   <li>T+2 date stored and retrieved correctly as a PostgreSQL DATE column</li>
 * </ul>
 */
@SpringBatchTest
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class SettlementJobPostgresIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("batchdb")
                    .withUsername("batch")
                    .withPassword("batch");

    /**
     * Override the DataSource URL/credentials to point to the Testcontainers instance.
     * The driver class is already {@code org.postgresql.Driver} in application.properties.
     */
    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        jdbcTemplate.execute("DELETE FROM settlements");
    }

    @Test
    void settlements_are_persisted_in_postgres(@TempDir Path tempDir) throws Exception {
        String inputFile = new ClassPathResource("test-data/trades_test.csv")
                .getFile().getAbsolutePath();

        JobExecution execution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addString("inputFile", inputFile)
                        .addString("runDate", "2026-05-21")
                        .addString("outputDir", tempDir.toAbsolutePath().toString())
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters()
        );

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Verify against real PostgreSQL — not possible with H2
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM settlements", Integer.class);
        assertThat(count).isEqualTo(4); // 5 trades - 1 CANCELLED

        // T+2: tradeDate 2026-05-21 → settlementDate 2026-05-23
        Integer t2Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM settlements WHERE settlement_date = '2026-05-23'",
                Integer.class);
        assertThat(t2Count).isEqualTo(4);

        // All settlements must be in SETTLED status
        Integer settledCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM settlements WHERE status = 'SETTLED'",
                Integer.class);
        assertThat(settledCount).isEqualTo(4);
    }
}
