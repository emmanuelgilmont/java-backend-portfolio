package be.gate25.tokencontext;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard error response body returned by {@link GlobalExceptionHandler}.
 *
 * <p>Example JSON output:
 * <pre>{@code
 * {
 *   "status": 500,
 *   "message": "An unexpected error occurred. Please contact the helpdesk.",
 *   "token": "a1b2c3d4-e5f6-..."
 * }
 * }</pre>
 *
 * <p>The {@code token} field allows the helpdesk to locate the exact request
 * in the log files and reconstruct the full transaction trace.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String message;
    private final String token;

    public ErrorResponse(int status, String message, String token) {
        this.status  = status;
        this.message = message;
        this.token   = token;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}