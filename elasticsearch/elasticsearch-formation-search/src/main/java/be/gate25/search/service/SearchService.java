package be.gate25.search.service;

import be.gate25.search.document.FormationDocument;
import be.gate25.search.dto.SearchRequest;
import be.gate25.search.dto.SearchResult;
import be.gate25.search.repository.FormationRepository;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations operations;
    private final FormationRepository repository;

    public List<SearchResult> search(SearchRequest request) {

        BoolQuery.Builder bool = new BoolQuery.Builder();

        // Full-text : content + meta.title (boost x3 sur le titre)
        if (request.q() != null && !request.q().isBlank()) {
            bool.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(request.q())
                    .fields("meta.title^3", "content")
                )
            ));
        }

        // Filtre extension
        if (request.extension() != null) {
            bool.filter(Query.of(q -> q
                .term(t -> t
                    .field("file.extension")
                    .value(request.extension())
                )
            ));
        }

        // Filtre date range sur last_modified
        if (request.from() != null || request.to() != null) {
            bool.filter(Query.of(q -> q
                .range(r -> r
                    .date(d -> {
                        d.field("file.last_modified");
                        if (request.from() != null) d.gte(request.from().toString());
                        if (request.to() != null)   d.lte(request.to().toString());
                        return d;
                    })
                )
            ));
        }

        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(bool.build())))
            .withHighlightQuery(highlight())
            .build();

        SearchHits<FormationDocument> hits = operations.search(query, FormationDocument.class);

        return hits.stream()
            .map(this::toResult)
            .toList();
    }

    public List<SearchResult> browse(String path) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .prefix(p -> p
                    .field("path.virtual")
                    .value(path)
                )
            ))
            .build();

        return operations.search(query, FormationDocument.class)
            .stream()
            .map(this::toResult)
            .toList();
    }

    public List<String> paths() {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q.matchAll(m -> m)))
            .withAggregation("paths", Aggregation.of(a -> a
                .terms(t -> t
                    .field("path.virtual.tree")
                    .size(1000)
                )
            ))
            .withPageable(PageRequest.of(0, 1))
            .build();

        SearchHits<FormationDocument> hits = operations.search(query, FormationDocument.class);

        ElasticsearchAggregations aggs = (ElasticsearchAggregations) hits.getAggregations();
        return aggs.get("paths")
            .aggregation()
            .getAggregate()
            .sterms()
            .buckets()
            .array()
            .stream()
            .map(b -> b.key().stringValue())
            .filter(p -> !p.matches(".*/[^/]+\\.[^/]+$"))
            .sorted()
            .toList();
    }

    // --- private ---

    private HighlightQuery highlight() {
        return new HighlightQuery(
            new Highlight(List.of(new HighlightField("content"))),
            FormationDocument.class
        );
    }

    private SearchResult toResult(SearchHit<FormationDocument> hit) {
        FormationDocument doc = hit.getContent();
        String snippet = hit.getHighlightFields()
            .getOrDefault("content", List.of())
            .stream()
            .findFirst()
            .orElse(null);

        return new SearchResult(
            hit.getId(),
            doc.getFile() != null ? doc.getFile().getFilename() : null,
            doc.getMeta() != null ? doc.getMeta().getTitle() : null,
            doc.getFile() != null ? doc.getFile().getExtension() : null,
            doc.getPath() != null ? doc.getPath().getVirtual() : null,
            doc.getFile() != null ? doc.getFile().getLastModified() : null,
            snippet
        );
    }
}