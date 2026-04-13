package be.gate25.search.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(indexName = "formations", createIndex = false)
public class FormationDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Object)
    private FileMetadata file;

    @Field(type = FieldType.Object)
    private DocMeta meta;

    @Field(type = FieldType.Object)
    private PathInfo path;

    @Data
    @NoArgsConstructor
    public static class FileMetadata {
        private String filename;
        private String extension;
        @Field(name = "content_type")
        private String contentType;
        private Long filesize;
        @Field(name = "last_modified",type = FieldType.Date, format = DateFormat.date_optional_time)
        private Instant lastModified;
        @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
        private Instant created;
    }

    @Data
    @NoArgsConstructor
    public static class DocMeta {
        private String title;
        private String author;
        private String keywords;
        private String description;
        private String language;
    }

    @Data
    @NoArgsConstructor
    public static class PathInfo {
        @Field(type = FieldType.Keyword)
        private String real;
        @Field(type = FieldType.Keyword)
        private String virtual;
    }
}