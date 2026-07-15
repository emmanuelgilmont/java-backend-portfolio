package be.gate25.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

/**
 * Logs a summary after the settlement job completes.
 */
@Component
public class JobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionListener.class);

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            long readCount = jobExecution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getReadCount).sum();
            long writeCount = jobExecution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getWriteCount).sum();
            long filterCount = jobExecution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getFilterCount).sum();

            log.info("=== Settlement Job COMPLETED ===");
            log.info("  Trades read         : {}", readCount);
            log.info("  Settlements written : {}", writeCount);
            log.info("  Filtered (CANCELLED): {}", filterCount);
        } else {
            log.warn("=== Settlement Job ended with status: {} ===", jobExecution.getStatus());
            jobExecution.getAllFailureExceptions()
                    .forEach(e -> log.error("  Failure: {}", e.getMessage()));
        }
    }
}
