package be.gate25.batch.domain;

/**
 * Outcome status of a processed trade settlement.
 */
public enum SettlementStatus {
    /** Trade was processed and settlement computed (T+2). */
    SETTLED,
    /** Trade was cancelled upstream — filtered out, not written. */
    CANCELLED,
    /** Processing failed (reserved for future error handling). */
    FAILED
}
