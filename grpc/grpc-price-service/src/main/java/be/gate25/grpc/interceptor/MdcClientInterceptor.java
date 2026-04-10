package be.gate25.grpc.interceptor;

import be.gate25.tokencontext.TokenFilter;
import io.grpc.*;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * gRPC client interceptor — propagates the transaction token from MDC to gRPC Metadata.
 *
 * <p>Runs on the Tomcat thread (where MDC is populated by {@code TokenFilter}).
 * Copies the token into the outbound gRPC call headers so the server interceptor
 * can restore it on the Netty thread.
 */
@GrpcGlobalClientInterceptor
public class MdcClientInterceptor implements ClientInterceptor {

    static final Metadata.Key<String> CORRELATION_KEY =
        Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method,
        CallOptions callOptions,
        Channel next) {

        String token = MDC.get(TokenFilter.MDC_TOKEN_KEY);

        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                if (token != null) {
                    headers.put(CORRELATION_KEY, token);
                }
                super.start(responseListener, headers);
            }
        };
    }
}