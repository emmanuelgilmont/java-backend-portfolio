package be.gate25.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Layer 1 — local test, no Docker required.
 * Verifies the Spring context (Eureka Server auto-configuration) loads correctly.
 */
@SpringBootTest
class EurekaServerApplicationTests {

    @Test
    void contextLoads() {
        // Context load is the assertion: fails if @EnableEurekaServer wiring is broken.
    }
}
