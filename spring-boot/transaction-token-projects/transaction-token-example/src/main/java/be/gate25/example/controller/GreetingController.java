package be.gate25.example.controller;

import be.gate25.example.service.GreetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example REST controller.
 *
 * Notice: zero token management here. The filter handles everything.
 * The developer just logs and throws — the starter does the rest.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GreetingController {

    private final GreetingService greetingService;

    /**
     * GET /api/greet?name=Alice
     * Happy path — returns a greeting. All log lines share the same token.
     */
    @GetMapping("/greet")
    public ResponseEntity<String> greet(@RequestParam(defaultValue = "World") String name) {
        log.info("GreetingController.greet() received request for name='{}'", name);
        String result = greetingService.greet(name);
        log.info("GreetingController.greet() sending response");
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/greet?name= (blank)
     * Triggers a 400 Bad Request with token in the response body.
     */
    @GetMapping("/fail-gracefully")
    public ResponseEntity<String> failGracefully() {
        log.info("GreetingController.failGracefully() called");
        return ResponseEntity.ok(greetingService.greet(""));   // will throw 400
    }

    /**
     * GET /api/fail-hard
     * Triggers an unexpected 500 — the client gets the token, never the stack trace.
     */
    @GetMapping("/fail-hard")
    public ResponseEntity<String> failHard() {
        log.info("GreetingController.failHard() called");
        return ResponseEntity.ok(greetingService.riskyOperation());   // will throw 500
    }
}