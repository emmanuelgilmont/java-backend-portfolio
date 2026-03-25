package be.gate25.weather.domain;

import java.util.List;

public record WeatherResponse(Place place, Current current, List<Daily> daily) {
    public record Place(String name, String postcode, String country, String timezone) {
    }

    public record Current(double temperatureC, double windKmh, int windDirectionDeg, double precipitationMm, int weatherCode,
        String weatherCodeMeaning) {
    }

    public record Daily(String date, double tempMaxC, double tempMinC, double precipSumMm, int precipProbMaxPct,
        double windMaxKmh, int weatherCode, String weatherCodeMeaning, double uvIndex, double windKmh, int windDirectionDeg) {
    }
}