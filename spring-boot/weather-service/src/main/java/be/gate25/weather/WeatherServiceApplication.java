package be.gate25.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WeatherServiceApplication.class, args);
        log.info("Weather service application started");
    }

}