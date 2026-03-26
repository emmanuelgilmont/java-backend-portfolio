package be.gate25.tokencontext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Servlet filter that initializes and clears the transaction token for each incoming request.
 *
 * <p>On every request:
 * <ul>
 *   <li>Generates a new UUID token, or reuses the {@code X-Correlation-ID} header if present.</li>
 *   <li>Stores the token in SLF4J MDC under the key {@code transactionToken}.</li>
 *   <li>Echoes the token back in the {@code X-Correlation-ID} response header.</li>
 *   <li>Always clears the MDC at the end of the request (even on exception),
 *       preventing token leakage across thread-pool reuse.</li>
 * </ul>
 *
 * <p>Once the filter is active, every {@code log.xxx()} call automatically includes
 * the token — no manual propagation needed in controllers or services.
 */
public class TokenFilter extends OncePerRequestFilter {

    /** MDC key used to store and retrieve the transaction token. */
    public static final String MDC_TOKEN_KEY = "transactionToken";

    /** HTTP header used to accept or propagate the correlation ID. */
    public static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = Optional
                    .ofNullable(request.getHeader(CORRELATION_HEADER))
                    .filter(h -> !h.isBlank())
                    .orElse(UUID.randomUUID().toString());

            MDC.put(MDC_TOKEN_KEY, token);

            // Echo the token in the response so the caller always knows it
            response.setHeader(CORRELATION_HEADER, token);

            filterChain.doFilter(request, response);

        } finally {
            // Critical: always clean up — thread pool reuse would carry over stale tokens
            MDC.clear();
        }
    }
}