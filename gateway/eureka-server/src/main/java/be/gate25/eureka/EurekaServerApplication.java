package be.gate25.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka service registry for the be.gate25 portfolio.
 * <p>
 * Standalone registry — no other application here. Downstream services
 * (fx-rate-service, grpc-price-service, weather-service, …) each need the
 * spring-cloud-starter-netflix-eureka-client dependency added before they
 * can register here and be routed to via {@code lb://SERVICE-ID} from the
 * gateway-service.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
