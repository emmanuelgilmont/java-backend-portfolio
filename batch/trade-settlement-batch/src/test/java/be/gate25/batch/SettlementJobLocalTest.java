package be.gate25.batch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Layer 1 — Local tests (no Docker required).
 *
 * <p>H2 in-memory replaces PostgreSQL for the JobRepository and the settlements table.
 * The same Spring Batch application context runs — only the DataSource changes.
 * Run with: {@code mvn test}
 *
 * <p>What this layer validates:
 * <ul>
 *   <li>The job completes successfully end-to-end</li>
 *   <li>CANCELLED trades are filtered (filterCount = 1)</li>
 *   <li>Remaining trades are written (writeCount = 4)</li>
 *   <li>The output CSV file is created</li>
 * </ul>
 */
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class SettlementJobLocalTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @AfterEach
    void cleanUp() {
        // Remove job execution metadata so each test starts fresh
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void job_completes_and_filters_cancelled_trades(@TempDir Path tempDir) throws Exception {
        String inputFile = new ClassPathResource("test-data/trades_test.csv")
                .getFile().getAbsolutePath();

        JobExecution execution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addString("inputFile", inputFile)
                        .addString("runDate", "2026-05-21")
                        .addString("outputDir", tempDir.toAbsolutePath().toString())
                        // Unique timestamp ensures the JobRepository accepts re-runs
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters()
        );

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(5);    // 5 rows in the CSV (excluding header)
        assertThat(step.getFilterCount()).isEqualTo(1);  // T003 is CANCELLED → filtered
        assertThat(step.getWriteCount()).isEqualTo(4);   // 4 settlements written
        assertThat(step.getSkipCount()).isZero();
    }

    @Test
    void output_csv_file_is_created(@TempDir Path tempDir) throws Exception {
        String inputFile = new ClassPathResource("test-data/trades_test.csv")
                .getFile().getAbsolutePath();

        jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addString("inputFile", inputFile)
                        .addString("runDate", "2026-05-21")
                        .addString("outputDir", tempDir.toAbsolutePath().toString())
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters()
        );

        Path csvOutput = tempDir.resolve("settlements_2026-05-21.csv");
        assertThat(csvOutput).exists();

        // Header + 4 data rows = 5 lines
        assertThat(java.nio.file.Files.lines(csvOutput).count()).isEqualTo(5);
    }
}
