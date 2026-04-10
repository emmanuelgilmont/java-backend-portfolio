package be.gate25.grpc;

import be.gate25.grpc.proto.PriceRequest;
import be.gate25.grpc.proto.PriceResponse;
import be.gate25.grpc.proto.PriceServiceGrpc;
import be.gate25.grpc.repository.PriceRepository;
import be.gate25.grpc.service.PriceServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for PriceServiceImpl.
 * Uses an in-process gRPC server — no network, no Spring context, fast.
 */
class PriceServiceIntegrationTest {

    private Server         server;
    private ManagedChannel channel;
    private PriceServiceGrpc.PriceServiceBlockingStub stub;

    @BeforeEach
    void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder
            .forName(serverName)
            .directExecutor()
            .addService(new PriceServiceImpl(new PriceRepository()))
            .build()
            .start();

        channel = InProcessChannelBuilder
            .forName(serverName)
            .directExecutor()
            .build();

        stub = PriceServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (channel != null) {
            channel.shutdownNow();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }

        if (server != null) {
            server.shutdownNow();
            server.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void getPrice_knownSymbol_returnsPrice() {
        PriceRequest request = PriceRequest.newBuilder()
                .setSymbol("BEL20:UCB")
                .build();

        PriceResponse response = stub.getPrice(request);

        assertThat(response.getSymbol()).isEqualTo("BEL20:UCB");
        assertThat(response.getPrice()).isPositive();
        assertThat(response.getCurrency()).isEqualTo("EUR");
        assertThat(response.getTimestamp()).isNotBlank();
    }

    @Test
    void getPrice_unknownSymbol_returnsNotFound() {
        PriceRequest request = PriceRequest.newBuilder()
                .setSymbol("UNKNOWN:XYZ")
                .build();

        assertThatThrownBy(() -> stub.getPrice(request))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(ex -> assertThat(
                    ((StatusRuntimeException) ex).getStatus().getCode()
                ).isEqualTo(Status.NOT_FOUND.getCode()));
    }
}