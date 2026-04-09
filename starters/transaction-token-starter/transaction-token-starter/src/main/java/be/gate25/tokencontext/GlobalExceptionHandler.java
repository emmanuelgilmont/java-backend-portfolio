package be.gate25.tokencontext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler that intercepts all unhandled exceptions and returns
 * a safe, structured {@link ErrorResponse} — never a stack trace.
 *
 * <p>The transaction token is retrieved once here from {@link TokenContext}
 * and included in the response body. The client can pass this token to the
 * helpdesk, who can then search the log files and reconstruct the full
 * request trace.
 *
 * <p>This bean is registered automatically by {@link TokenContextAutoConfiguration}.
 * It can be disabled via {@code token-context.exception-handler.enabled=false}
 * if the application provides its own {@code @RestControllerAdvice}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String HELPDESK_MESSAGE =
            "An unexpected error occurred. Please contact the helpdesk with your token.";

    /**
     * Handles Spring's {@link ResponseStatusException} (e.g. 404, 403, 400)
     * and returns the appropriate HTTP status with a structured body.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String token = TokenContext.getToken();
        log.warn("ResponseStatusException [token={}]: {} {}", token, ex.getStatusCode(), ex.getReason());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ErrorResponse(ex.getStatusCode().value(), ex.getReason(), token));
    }

    /**
     * Catch-all for any unhandled exception.
     * The stack trace is logged server-side; the client only receives the token.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        String token = TokenContext.getToken();
        // Full stack trace in the logs — never sent to the client
        log.error("Unhandled exception [token={}]", token, ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HELPDESK_MESSAGE, token));
    }
}