package be.gate25.fxrate.controller;

import be.gate25.fxrate.domain.FxRate;
import be.gate25.fxrate.provider.UnsupportedPairException;
import be.gate25.fxrate.service.RateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for FX rates.
 *
 * <pre>
 *   GET  /rates              → list supported pairs
 *   GET  /rates/{pair}       → get (possibly cached) rate for a pair
 *   DELETE /rates/{pair}     → evict single pair from cache
 *   DELETE /rates            → evict all pairs from cache
 * </pre>
 */
@RestController
@RequestMapping("/rates")
public class RateController {

    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @GetMapping
    public List<String> getSupportedPairs() {
        return rateService.getSupportedPairs();
    }

    @GetMapping("/{pair}")
    public ResponseEntity<FxRate> getRate(@PathVariable String pair) {
        // Path variables use '-' as separator (URL-safe), normalise to '/'
        String normalisedPair = pair.replace("-", "/").toUpperCase();
        FxRate rate = rateService.getRate(normalisedPair);
        return ResponseEntity.ok(rate);
    }

    @DeleteMapping("/{pair}")
    public ResponseEntity<Void> evictRate(@PathVariable String pair) {
        String normalisedPair = pair.replace("-", "/").toUpperCase();
        rateService.evictRate(normalisedPair);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> evictAll() {
        rateService.evictAll();
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------------------
    // Error handling
    // ---------------------------------------------------------------------------

    @ExceptionHandler(UnsupportedPairException.class)
    public ResponseEntity<String> handleUnsupportedPair(UnsupportedPairException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
