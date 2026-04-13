package be.gate25.search.repository;

import be.gate25.search.document.FormationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface FormationRepository extends ElasticsearchRepository<FormationDocument, String> {

    List<FormationDocument> findByFileExtension(String extension);

    List<FormationDocument> findByMetaLanguage(String language);
}