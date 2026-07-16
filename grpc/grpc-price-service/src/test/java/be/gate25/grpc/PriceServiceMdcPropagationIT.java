package be.gate25.grpc;

import be.gate25.grpc.controller.PriceController;
import be.gate25.grpc.service.PriceServiceImpl;
import be.gate25.tokencontext.TokenFilter;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test: real embedded Tomcat + real network gRPC server
 * (grpc-server-spring-boot-starter auto-configuration, Netty transport), so the
 * MDC interceptors registered via Spring Boot auto-configuration are actually
 * exercised — unlike {@link PriceServiceIntegrationTest}, which wires an
 * in-process gRPC server by hand and therefore never registers those interceptors.
 *
 * <p>Verifies that the transaction token set in MDC by {@code TokenFilter} on the
 * Tomcat request thread is restored into MDC on the gRPC/Netty thread that runs
 * {@link PriceServiceImpl}, via {@code MdcClientInterceptor} / {@code MdcServerInterceptor}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PriceServiceMdcPropagationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private Logger priceControllerLogger;
    private Logger priceServiceLogger;
    private ListAppender<ILoggingEvent> priceControllerAppender;
    private ListAppender<ILoggingEvent> priceServiceAppender;

    @BeforeEach
    void attachLogAppenders() {
        priceControllerLogger = (Logger) LoggerFactory.getLogger(PriceController.class);
        priceServiceLogger = (Logger) LoggerFactory.getLogger(PriceServiceImpl.class);

        priceControllerAppender = new ListAppender<>();
        priceControllerAppender.start();
        priceControllerLogger.addAppender(priceControllerAppender);

        priceServiceAppender = new ListAppender<>();
        priceServiceAppender.start();
        priceServiceLogger.addAppender(priceServiceAppender);
    }

    @AfterEach
    void detachLogAppenders() {
        priceControllerLogger.detachAppender(priceControllerAppender);
        priceServiceLogger.detachAppender(priceServiceAppender);
    }

    @Test
    void transactionToken_isRestoredInMdc_acrossTomcatToNettyBoundary() {
        String token = UUID.randomUUID().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(TokenFilter.CORRELATION_HEADER, token);

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/price/BEL20:UCB",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        ILoggingEvent controllerEvent = priceControllerAppender.list.stream()
            .filter(e -> e.getFormattedMessage().contains("REST request for symbol"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("PriceController did not log the expected message"));

        ILoggingEvent serviceEvent = priceServiceAppender.list.stream()
            .filter(e -> e.getFormattedMessage().contains("Received GetPrice request"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("PriceServiceImpl did not log the expected message"));

        // Sanity check: the two log lines must actually originate from different
        // threads (Tomcat worker vs. gRPC/Netty), otherwise the token being present
        // on both would prove nothing about the MDC restoration this test targets.
        assertThat(serviceEvent.getThreadName())
            .as("PriceServiceImpl must run on the gRPC transport thread, not the Tomcat request thread")
            .isNotEqualTo(controllerEvent.getThreadName());

        assertThat(controllerEvent.getMDCPropertyMap())
            .as("MDC on the Tomcat thread must carry the token set by TokenFilter")
            .containsEntry(TokenFilter.MDC_TOKEN_KEY, token);

        assertThat(serviceEvent.getMDCPropertyMap())
            .as("MDC on the gRPC/Netty thread must carry the token restored by MdcServerInterceptor")
            .containsEntry(TokenFilter.MDC_TOKEN_KEY, token);
    }
}
