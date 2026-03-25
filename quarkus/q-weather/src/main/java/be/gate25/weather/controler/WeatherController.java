package be.gate25.weather.controler;

import be.gate25.weather.domain.WeatherResponse;
import be.gate25.weather.service.WeatherService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/v1")
@ApplicationScoped
public class WeatherController {
    private final WeatherService service;

    public WeatherController(WeatherService service) {
        this.service = service;
    }

    @GET
    @Path("/weather")
    @Produces(MediaType.APPLICATION_JSON)
    public WeatherResponse weather(
        @QueryParam("place") String place,
        @QueryParam("days") @DefaultValue("3") @Min(1) @Max(3) int days) {
        return service.getWeather(place, days);
    }

    @GET
    @Path("/weather/default")
    @Produces(MediaType.APPLICATION_JSON)
    public WeatherResponse weatherDefault() {
        return service.getDefaultWeather();
    }

    @GET
    @Path( "/weather/human")
    @Produces(MediaType.TEXT_PLAIN)
    public String humanDaily(@QueryParam("place") String place) {
        return service.getHumanDaily(place);
    }
}