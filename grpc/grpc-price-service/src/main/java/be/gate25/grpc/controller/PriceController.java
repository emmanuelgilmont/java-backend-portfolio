package be.gate25.grpc.controller;

import be.gate25.grpc.proto.PriceRequest;
import be.gate25.grpc.proto.PriceResponse;
import be.gate25.grpc.proto.PriceServiceGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST → gRPC bridge.
 * Exposes a simple HTTP endpoint that delegates to the local PriceService via gRPC.
 *
 * GET /price/{symbol}  →  PriceServiceGrpc.GetPrice(symbol)
 */
@RestController
@RequestMapping("/price")
public class PriceController {

    private static final Logger log = LoggerFactory.getLogger(PriceController.class);

    @GrpcClient("price-service")
    private PriceServiceGrpc.PriceServiceBlockingStub priceServiceStub;

    @GetMapping("/{symbol}")
    public ResponseEntity<?> getPrice(@PathVariable String symbol) {
        log.debug("REST request for symbol: {}", symbol);

        try {
            PriceResponse response = priceServiceStub.getPrice(
                PriceRequest.newBuilder().setSymbol(symbol).build()
            );
            return ResponseEntity.ok(Map.of(
                "symbol",    response.getSymbol(),
                "price",     response.getPrice(),
                "currency",  response.getCurrency(),
                "timestamp", response.getTimestamp()
            ));
        } catch (StatusRuntimeException e) {
            log.warn("gRPC error for symbol {}: {}", symbol, e.getStatus());
            return switch (e.getStatus().getCode()) {
                case NOT_FOUND -> ResponseEntity.status(404)
                    .body(Map.of("error", "No price available for symbol: " + symbol));
                default -> ResponseEntity.status(500)
                    .body(Map.of("error", "Unexpected error: " + e.getStatus().getDescription()));
            };
        }
    }
}
