package be.gate25.grpc.interceptor;

import be.gate25.tokencontext.TokenFilter;
import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;

/**
 * gRPC server interceptor — restores the transaction token from gRPC Metadata into MDC.
 *
 * <p>Runs on the Netty thread (where MDC is empty by default).
 * The token is set/cleared on each Listener callback — not just at startCall() time —
 * because gRPC processes the actual request inside onHalfClose(), which is invoked
 * after startCall() returns.
 */
@GrpcGlobalServerInterceptor
public class MdcServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next) {

        String token = headers.get(MdcClientInterceptor.CORRELATION_KEY);

        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {

            @Override
            public void onMessage(ReqT message) {
                withMdc(() -> super.onMessage(message));
            }

            @Override
            public void onHalfClose() {
                withMdc(super::onHalfClose);
            }

            @Override
            public void onCancel() {
                withMdc(super::onCancel);
            }

            @Override
            public void onComplete() {
                withMdc(super::onComplete);
            }

            private void withMdc(Runnable action) {
                if (token != null) {
                    MDC.put(TokenFilter.MDC_TOKEN_KEY, token);
                }
                try {
                    action.run();
                } finally {
                    MDC.remove(TokenFilter.MDC_TOKEN_KEY);
                }
            }
        };
    }
}