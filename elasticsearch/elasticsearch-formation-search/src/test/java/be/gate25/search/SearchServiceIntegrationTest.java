package be.gate25.search;

import be.gate25.search.document.FormationDocument;
import be.gate25.search.dto.SearchRequest;
import be.gate25.search.dto.SearchResult;
import be.gate25.search.service.SearchService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchServiceIntegrationTest {

    @Container
    @ServiceConnection
    static ElasticsearchContainer es = new ElasticsearchContainer(
        "docker.elastic.co/elasticsearch/elasticsearch:8.19.8"
    ).withEnv("xpack.security.enabled", "false");

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private SearchService searchService;

    @BeforeAll
    void setup() {
        // Create the index and seed test documents
        operations.indexOps(FormationDocument.class).createWithMapping();

        index("1", "Introduction to Spring Boot", "Spring Boot makes it easy to create stand-alone applications.", "pdf", "/formations/spring");
        index("2", "Apache Kafka in Practice",    "Kafka is a distributed event streaming platform.",              "pdf", "/formations/kafka");
        index("3", "Docker for Developers",       "Docker containers package your application and its dependencies.", "mp4", "/webinaires/docker");

        // Allow ES to index
        operations.indexOps(FormationDocument.class).refresh();
    }

    @Test
    void searchByKeyword_shouldReturnMatchingDocuments() {
        List<SearchResult> results = searchService.search(new SearchRequest("spring", null, null, null));
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(r -> r.filename().contains("spring-boot"));
    }

    @Test
    void searchByExtension_shouldFilterCorrectly() {
        List<SearchResult> results = searchService.search(new SearchRequest(null, "mp4", null, null));
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().extension()).isEqualTo("mp4");
    }

    @Test
    void searchByDateRange_shouldReturnDocumentsInRange() {
        List<SearchResult> results = searchService.search(new SearchRequest(
            null, null,
            Instant.parse("2020-01-01T00:00:00Z"),
            Instant.now()
        ));
        assertThat(results).isNotEmpty();
    }

    @Test
    void browse_shouldReturnDocumentsUnderPath() {
        List<SearchResult> results = searchService.browse("/formations");
        assertThat(results).hasSize(2);
    }

    @Test
    void paths_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> searchService.paths());
    }

    // --- helper ---

    private void index(String id, String title, String content, String extension, String path) {
        FormationDocument doc = new FormationDocument();
        doc.setId(id);
        doc.setContent(content);

        FormationDocument.FileMetadata file = new FormationDocument.FileMetadata();
        file.setFilename(title.toLowerCase().replace(" ", "-") + "." + extension);
        file.setExtension(extension);
        file.setLastModified(Instant.now());
        doc.setFile(file);

        FormationDocument.DocMeta meta = new FormationDocument.DocMeta();
        meta.setTitle(title);
        doc.setMeta(meta);

        FormationDocument.PathInfo pathInfo = new FormationDocument.PathInfo();
        pathInfo.setVirtual(path + "/" + file.getFilename());
        doc.setPath(pathInfo);

        operations.save(doc);
    }
}