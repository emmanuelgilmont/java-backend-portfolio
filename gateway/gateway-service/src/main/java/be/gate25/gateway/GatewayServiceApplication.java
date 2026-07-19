package be.gate25.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway for the be.gate25 portfolio.
 * <p>
 * Routes are defined in {@code application.properties} using the
 * {@code spring.cloud.gateway.server.webflux.routes[n]} property namespace
 * (Spring Cloud Gateway 2025.1.x — reactive/webflux variant).
 * <p>
 * Registers itself with eureka-server on startup so its own health/status is
 * visible in the registry, even though downstream routes currently use
 * static URIs (see the NOTE in application.properties).
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
