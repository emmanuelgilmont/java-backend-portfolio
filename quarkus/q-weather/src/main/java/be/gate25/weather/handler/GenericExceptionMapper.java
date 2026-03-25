package be.gate25.weather.handler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception ex) {
        log.error("Upstream error occurred: {}", ex.getMessage(), ex);
        return Response
            .status(Response.Status.BAD_GATEWAY)
                .entity(Map.of("title", "Upstream error", "detail", ex.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}