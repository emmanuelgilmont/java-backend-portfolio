package be.gate25.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Layer 1 — local test, no Docker required.
 * Verifies the Spring context (Gateway route definitions, Eureka client
 * auto-configuration) loads correctly. Does not require eureka-server or
 * any downstream service to be running.
 */
@SpringBootTest
class GatewayServiceApplicationTests {

    @Test
    void contextLoads() {
        // Context load is the assertion: fails if route property binding is malformed.
    }
}
