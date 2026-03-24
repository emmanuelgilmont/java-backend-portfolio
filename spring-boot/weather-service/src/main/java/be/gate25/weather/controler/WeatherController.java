package be.gate25.weather.controler;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.gate25.weather.domain.WeatherResponse;
import be.gate25.weather.service.WeatherService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/v1")
@Validated
public class WeatherController {
    private final WeatherService service;

    public WeatherController(WeatherService service) {
        this.service = service;
    }

    @GetMapping("/weather")
    public WeatherResponse weather(@RequestParam(required = false) String place,
        @RequestParam(defaultValue = "3") @Min(1) @Max(3) int days) {
        return service.getWeather(place, days);
    }

    @GetMapping("/weather/default")
    public WeatherResponse weatherDefault() {
        return service.getDefaultWeather();
    }

    @GetMapping(value = "/weather/human", produces = MediaType.TEXT_PLAIN_VALUE)
    public String humanDaily(@RequestParam(required = false) String place) {
        return service.getHumanDaily(place);
    }
}