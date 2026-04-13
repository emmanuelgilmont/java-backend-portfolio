package be.gate25.search.controller;

import be.gate25.search.dto.SearchRequest;
import be.gate25.search.dto.SearchResult;
import be.gate25.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public List<SearchResult> search(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String extension,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to
    ) {
        return searchService.search(new SearchRequest(q, extension, from, to));
    }

    @GetMapping("/browse")
    public List<SearchResult> browse(
        @RequestParam String path
    ) {
        return searchService.browse(path);
    }

    @GetMapping("/paths")
    public List<String> paths() {
        return searchService.paths();
    }
}