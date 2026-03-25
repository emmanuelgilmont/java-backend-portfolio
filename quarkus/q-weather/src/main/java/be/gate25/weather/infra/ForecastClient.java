package be.gate25.weather.infra;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@RegisterRestClient(configKey = "forecast-api")
public interface ForecastClient {
    @GET
    @Path("/forecast")
    @Produces(MediaType.APPLICATION_JSON)
    JsonNode forecast(@QueryParam("latitude") double lat, @QueryParam("longitude") double lon,
        @QueryParam("timezone") String timezone, @QueryParam("forecast_days") int forecastDays,
        @QueryParam("current") String current, @QueryParam("daily") String daily);
}