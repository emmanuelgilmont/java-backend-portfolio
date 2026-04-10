package be.gate25.grpc.service;

import be.gate25.grpc.model.Price;
import be.gate25.grpc.proto.PriceRequest;
import be.gate25.grpc.proto.PriceResponse;
import be.gate25.grpc.proto.PriceServiceGrpc;
import be.gate25.grpc.repository.PriceRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class PriceServiceImpl extends PriceServiceGrpc.PriceServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(PriceServiceImpl.class);

    private final PriceRepository priceRepository;

    public PriceServiceImpl(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public void getPrice(PriceRequest request, StreamObserver<PriceResponse> responseObserver) {
        String symbol = request.getSymbol();
        log.debug("Received GetPrice request for symbol: {}", symbol);

        Price price = priceRepository.findBySymbol(symbol);

        if (price == null) {
            log.warn("No price found for symbol: {}", symbol);
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription("No price available for symbol: " + symbol)
                    .asRuntimeException()
            );
            return;
        }

        PriceResponse response = PriceResponse.newBuilder()
                .setSymbol(price.getSymbol())
                .setPrice(price.getValue())
                .setCurrency(price.getCurrency())
                .setTimestamp(price.getTimestamp().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        log.debug("GetPrice response sent for symbol: {}", symbol);
    }
}