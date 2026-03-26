package be.gate25.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Example service demonstrating log correlation without any manual token handling.
 *
 * Notice: there is no mention of "token" anywhere in this class.
 * Yet every log line will be prefixed with the transaction token automatically.
 */
@Slf4j
@Service
public class GreetingService {

    public String greet(String name) {
        log.debug("GreetingService.greet() called with name='{}'", name);

        if (name == null || name.isBlank()) {
            log.warn("GreetingService.greet() rejected: name is blank");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be blank");
        }

        String greeting = "Hello, " + name + "!";
        log.info("GreetingService.greet() returning: '{}'", greeting);
        return greeting;
    }

    public String riskyOperation() {
        log.info("GreetingService.riskyOperation() starting");
        log.debug("GreetingService.riskyOperation() doing step 1");
        log.debug("GreetingService.riskyOperation() doing step 2");

        // Simulates an unexpected failure deep in the call stack
        log.error("GreetingService.riskyOperation() something went wrong");
        throw new RuntimeException("Simulated unexpected failure — check logs with your token");
    }
}