package be.gate25.search.dto;

import java.time.Instant;

public record SearchResult(
    String id,
    String filename,
    String title,
    String extension,
    String path,
    Instant lastModified,
    String highlight
) {
}