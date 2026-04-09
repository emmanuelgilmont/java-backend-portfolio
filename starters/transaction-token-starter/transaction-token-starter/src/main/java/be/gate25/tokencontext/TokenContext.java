package be.gate25.tokencontext;

import org.slf4j.MDC;

/**
 * Static facade providing explicit access to the current transaction token.
 *
 * <p>In the vast majority of cases you do NOT need this class — the token is
 * automatically injected into every log line via SLF4J MDC.
 *
 * <p>Use {@code TokenContext.getToken()} only when you need the token value
 * explicitly, typically to include it in an error response returned to the client.
 *
 * <pre>{@code
 * // Typical usage in a @RestControllerAdvice:
 * String token = TokenContext.getToken();
 * return new ErrorResponse(500, "Contact helpdesk", token);
 * }</pre>
 */
public final class TokenContext {

    private TokenContext() {
        // Utility class — not instantiable
    }

    /**
     * Returns the transaction token associated with the current request thread,
     * or {@code null} if called outside of a request context (e.g. in a test
     * without the filter).
     */
    public static String getToken() {
        return MDC.get(TokenFilter.MDC_TOKEN_KEY);
    }
}