package be.gate25.weather.handler;

import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NoSuchElementException> {

    @Override
    public Response toResponse(NoSuchElementException ex) {
        return Response
            .status(Response.Status.NOT_FOUND)
                .entity(Map.of("title", "Place not found", "detail", ex.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}