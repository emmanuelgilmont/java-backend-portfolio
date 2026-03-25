package be.gate25.weather.infra;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@RegisterRestClient(configKey = "geocoding-api")
public interface GeocodingClient {
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    JsonNode search(@QueryParam("name") String name, @QueryParam("count") int count, @QueryParam("language") String language,
        @QueryParam("format") String format, @QueryParam("countryCode") String countryCode);
}