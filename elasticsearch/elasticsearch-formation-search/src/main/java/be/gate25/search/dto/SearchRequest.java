package be.gate25.search.dto;

import java.time.Instant;

public record SearchRequest(
    String q,
    String extension,
    Instant from,
    Instant to
) {
}